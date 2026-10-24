package at.jku.isse.ecco.adapter.sysmlv2.viewer;

import at.jku.isse.ecco.adapter.ArtifactViewer;
import at.jku.isse.ecco.adapter.dispatch.PluginArtifactData;
import at.jku.isse.ecco.adapter.sysmlv2.SysMLFileWriter;
import at.jku.isse.ecco.adapter.sysmlv2.SysMLModule;
import at.jku.isse.ecco.adapter.sysmlv2.SysMLPlugin;
import at.jku.isse.ecco.tree.Node;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;

/**
 * Main functionality and structure copied from {@link at.jku.isse.ecco.adapter.lilypond.view.SVGViewer SVGViewer}
 * Used to inspect
 */
public class SysMLSVGViewer extends BorderPane implements ArtifactViewer {
    private static final double ZOOM_STEP = 1.15;
    private static final double MIN_ZOOM = 0.25;
    private static final double MAX_ZOOM = 4.0;

    private WebView webView;
    private double zoom = 1.0;

    private double dragStartX;
    private double dragStartY;

    private final Label zoomLabel = new Label();
    private final Button zoomOutButton = new Button("−");
    private final Button zoomInButton = new Button("+");
    private final Button zoomResetButton = new Button("Reset Zoom");
    private final HBox navigationBar;

    public SysMLSVGViewer() {
        zoomOutButton.setOnAction(e -> setZoom(this.zoom / ZOOM_STEP));
        zoomInButton.setOnAction(e -> setZoom(this.zoom * ZOOM_STEP));
        zoomResetButton.setOnAction(e -> setZoom(1.0));

        zoomLabel.setText(Math.round(this.zoom * 100) + "%");

        navigationBar = new HBox(10, zoomOutButton, zoomLabel, zoomInButton, zoomResetButton);
        navigationBar.setAlignment(Pos.CENTER);
        navigationBar.setPadding(new Insets(5));
    }

    private void setZoom(double newZoom) {
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));

        if (this.webView != null) {
            try {
                this.webView.getEngine().executeScript("setZoomLevel(" + this.zoom + ");");
            } catch (Exception ignored) {
            }
        }
        zoomLabel.setText(Math.round(this.zoom * 100) + "%");
    }

    @Override
    public String getPluginId() {
        return SysMLPlugin.class.getName();
    }

    @Override
    public String getTabName() {
        return "SysML";
    }

    @Override
    public void showTree(Node node) {
        if (node.getArtifact().getData() instanceof PluginArtifactData) {
            setCursor(Cursor.WAIT);
            this.setTop(null);
            this.setCenter(new ProgressIndicator());
            this.setBackground(Background.EMPTY);

            Thread th = new Thread(() -> {
                String sysMLText;
                synchronized (SysMLModule.sysMillLock) {
                    sysMLText = SysMLFileWriter.getSysML(node);
                }
                String svg = PlantUMLVisitor.getSVG(sysMLText);

                Platform.runLater(() -> {
                    if (svg != null) {
                        this.setTop(navigationBar);
                        this.setCenter(webView());
                        showSVG(svg);
                        this.setBackground(Background.EMPTY);
                    }
                    setCursor(Cursor.DEFAULT);
                });
            });
            th.start();
        }
    }

    private WebView webView() {
        if (this.webView == null) {
            this.webView = new WebView();
            webView.setContextMenuEnabled(false);

            // Zooms towards mouse when using scrollwheel
            this.webView.addEventFilter(ScrollEvent.SCROLL, event -> {
                double zoomFactor = event.getDeltaY() > 0 ? ZOOM_STEP : (1 / ZOOM_STEP);
                double newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, this.zoom * zoomFactor));

                if (newZoom != this.zoom) {
                    this.zoom = newZoom;
                    try {
                        webView.getEngine().executeScript(
                                "zoomTo(" + this.zoom + ", " + event.getX() + ", " + event.getY() + ");"
                        );
                    } catch (Exception e) {
                    }
                    zoomLabel.setText(Math.round(this.zoom * 100) + "%");
                }
                event.consume();
            });

            // Mouse dragging logic
            this.webView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.isPrimaryButtonDown()) {
                    dragStartX = event.getScreenX();
                    dragStartY = event.getScreenY();
                    webView.setCursor(Cursor.CLOSED_HAND);
                }
            });

            this.webView.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                webView.setCursor(Cursor.DEFAULT);
            });

            this.webView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
                if (event.isPrimaryButtonDown()) {
                    double deltaX = event.getScreenX() - dragStartX;
                    double deltaY = event.getScreenY() - dragStartY;

                    dragStartX = event.getScreenX();
                    dragStartY = event.getScreenY();

                    try {
                        // somehow only works if the webView script is called like this
                        webView.getEngine().executeScript("panBy(" + deltaX + ", " + deltaY + ");");
                    } catch (Exception ignored) {
                    }
                    event.consume();
                }
            });
        }
        return this.webView;
    }

    /**
     * Plain HTML with some JavaScript for the zooming logic.
     * The SVG String is inserted via string formatting
     *
     * @param svgString The SVG file as a String.
     */
    private void showSVG(String svgString) {
        final String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            background-color: transparent;
                            overflow: hidden;
                
                            user-select: none;
                            -webkit-user-select: none;
                            -webkit-user-drag: none;
                        }
                        #svg-container {
                            position: absolute;
                            transform-origin: 0 0;
                            pointer-events: none;
                        }
                        svg {
                            display: block;
                            pointer-events: none;
                        }
                    </style>
                    <script>
                        let currentZoom = 1.0;
                        let panX = 0;
                        let panY = 0;
                
                        function updateTransform() {
                            document.getElementById('svg-container').style.transform = 
                                `translate(${panX}px, ${panY}px) scale(${currentZoom})`;
                        }
                
                        function setZoomLevel(z) {
                            let centerX = window.innerWidth / 2;
                            let centerY = window.innerHeight / 2;
                            zoomTo(z, centerX, centerY);
                        }
                
                        function zoomTo(newZoom, mouseX, mouseY) {
                            let dx = (mouseX - panX) / currentZoom;
                            let dy = (mouseY - panY) / currentZoom;
                
                            currentZoom = newZoom;
                
                            panX = mouseX - dx * currentZoom;
                            panY = mouseY - dy * currentZoom;
                
                            updateTransform();
                        }
                
                        function panBy(dx, dy) {
                            panX += dx;
                            panY += dy;
                            updateTransform();
                        }
                
                        function initCenter() {
                            let container = document.getElementById('svg-container');
                            let svgNode = document.querySelector('svg');
                            if(svgNode) {
                                let rect = svgNode.getBoundingClientRect();
                                panX = (window.innerWidth - (rect.width * currentZoom)) / 2;
                                panY = (window.innerHeight - (rect.height * currentZoom)) / 2;
                                updateTransform();
                            }
                        }
                    </script>
                </head>
                <body onload="currentZoom = %s; initCenter();">
                    <div id="svg-container">
                        %s
                    </div>
                </body>
                </html>
                """.formatted(String.valueOf(this.zoom), svgString);

        webView.getEngine().loadContent(htmlContent);
    }
}