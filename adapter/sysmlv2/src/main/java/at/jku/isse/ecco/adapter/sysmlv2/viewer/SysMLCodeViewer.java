package at.jku.isse.ecco.adapter.sysmlv2.viewer;

import at.jku.isse.ecco.adapter.AssociationInfo;
import at.jku.isse.ecco.adapter.AssociationInfoArtifactViewer;
import at.jku.isse.ecco.adapter.dispatch.PluginArtifactData;
import at.jku.isse.ecco.adapter.sysmlv2.ASTNodeArtifactData;
import at.jku.isse.ecco.adapter.sysmlv2.SysMLModule;
import at.jku.isse.ecco.adapter.sysmlv2.SysMLPlugin;
import at.jku.isse.ecco.adapter.view.AssociationColors;
import at.jku.isse.ecco.adapter.view.FileTabs;
import at.jku.isse.ecco.core.Association;
import at.jku.isse.ecco.tree.Node;
import de.monticore.ast.ASTNode;
import de.monticore.lang.sysmlv2.SysMLv2Mill;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static at.jku.isse.ecco.adapter.sysmlv2.ASTSerializer.deserialize;

/**
 * Main functionality and structure copied from {@link at.jku.isse.ecco.adapter.text.TextViewer TextViewer}
 *
 */
public class SysMLCodeViewer extends BorderPane implements AssociationInfoArtifactViewer {
    private final AssociationColors colors = new AssociationColors(this::recolor);
    private final Map<Path, FileView> fileViews = new LinkedHashMap<>();
    private boolean highlightSelection = true;

    public SysMLCodeViewer() {
        this.setCenter(createListView(FXCollections.observableArrayList()));
    }

    private void recolor(String id, Color color) {
        for (FileView fv : fileViews.values()) {
            for (SysMLComponent row : fv.lines) {
                if (row.getAssociation() != null && id.equals(row.getAssociation().getId())) {
                    row.backgroundColor().set(color);
                }
            }
            fv.listView.refresh();
        }
    }

    @Override
    public void setAssociationInfos(Collection<AssociationInfo> associationInfos) {
        fileViews.clear();
        colors.set(associationInfos);
    }

    @Override
    public String getPluginId() {
        return SysMLPlugin.class.getName();
    }

    @Override
    public void showTree(Node node) {
        List<Node> fileNodes = FileTabs.files(node, getPluginId());
        Node selectedFileNode = FileTabs.fileOf(node, getPluginId());

        Map<Path, FileView> newFileViews = new LinkedHashMap<>();
        FileView selectedFileView = null;

        for (Node fileNode : fileNodes) {
            PluginArtifactData pad = (PluginArtifactData) fileNode.getArtifact().getData();
            Path fileName = pad.getPath();

            FileView fv = fileViews.computeIfAbsent(fileName, FileView::new);
            newFileViews.put(fileName, fv);

            if (fileNode.equals(selectedFileNode)) {
                fv.showFile(fileNode, node);
                selectedFileView = fv;
            } else {
                fv.showLater(fileNode);
            }
        }

        fileViews.clear();
        fileViews.putAll(newFileViews);

        this.setCenter(FileTabs.show(new ArrayList<>(newFileViews.values()), selectedFileView, fv -> fv.path.toString(),
                fv -> fv.shown().listView, () -> createListView(FXCollections.observableArrayList())));
    }

    private class FileView {
        private final Path path;
        private final ObservableList<SysMLComponent> lines = FXCollections.observableArrayList();
        private final ListView<SysMLComponent> listView = createListView(lines);
        private Node pending;

        FileView(Path path) {
            this.path = path;
        }

        void showLater(Node fileRoot) {
            this.pending = fileRoot;
        }

        FileView shown() {
            if (this.pending != null)
                this.showFile(this.pending, null);
            return this;
        }

        void showFile(Node fileRoot, Node nodeToHighlight) {
            this.pending = null;
            lines.clear();
            flattenTree(fileRoot, 0);
            if (nodeToHighlight != null) {
                highlight(nodeToHighlight);
            }
        }

        private void flattenTree(Node node, int depth) {
            for (Node child : node.getChildren()) {
                SysMLComponent comp = new SysMLComponent(child, colors.colorFor(child), false, depth);
                ASTNode astNode = comp.getSysNode();

                if (astNode != null) {
                    lines.add(comp);

                    // Check if the Ecco tree node has children. If it does, it acts as a block
                    // (e.g. a Package, or a Part containing Attributes), so we recurse and append a closing brace.
                    if (!child.getChildren().isEmpty()) {
                        flattenTree(child, depth + 1);
                        lines.add(new SysMLComponent(child, colors.colorFor(child), true, depth));
                    }
                } else {
                    // Non-AST structural nodes
                    flattenTree(child, depth);
                }
            }
        }

        void highlight(Node node) {
            int idx = -1;
            for (int i = 0; i < lines.size(); i++) {
                SysMLComponent row = lines.get(i);
                boolean match = row.getNode().equals(node);
                row.setHighlighted(match && highlightSelection);
                if (match) idx = i;
            }
            if (idx >= 0) {
                listView.scrollTo(idx);
                listView.getSelectionModel().select(idx);
            }
        }
    }

    private ListView<SysMLComponent> createListView(ObservableList<SysMLComponent> lines) {
        ListView<SysMLComponent> listView = new ListView<>(lines);
        listView.setFocusTraversable(false);
        listView.setCellFactory(lv -> new ListCell<>() {
            {
                setMaxWidth(Double.MAX_VALUE);
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(SysMLComponent row, boolean empty) {
                SysMLComponent old = getItem();
                if (old != null) {
                    backgroundProperty().unbind();
                }

                super.updateItem(row, empty);

                if (empty || row == null) {
                    setText(null);
                    setGraphic(null);
                    setBackground(null);
                } else {
                    setText(null);

                    String indent = "    ".repeat(Math.max(0, row.getDepth()));

                    if (row.isClosingBrace()) {
                        setGraphic(new Text(indent + "}"));
                    } else if (row.getSysNode() != null) {
                        String printed;
                        synchronized (SysMLModule.sysMillLock){
                            printed = SysMLv2Mill.prettyPrint(row.getSysNode(), true).trim();
                        }

                        // If the underlying Ecco node has children, only take the first line
                        // to prevent duplication of the nested child elements.
                        if (!row.getNode().getChildren().isEmpty()) {
                            printed = printed.split("\\r?\\n")[0].trim();
                            // Ensure the block opens properly
                            if (!printed.endsWith("{")) {
                                printed += " {";
                            }
                        } else {
                            // If it has no children, it's a leaf node. Convert empty blocks to semicolons.
                            printed = printed.replaceAll("\\s*\\{\\s*\\}", ";");
                        }

                        String indentedPrinted = Arrays.stream(printed.split("\\r?\\n"))
                                .map(line -> indent + line)
                                .collect(Collectors.joining("\n"));

                        setGraphic(new Text(indentedPrinted));
                    } else {
                        setGraphic(new Text(indent + row.getNode().toString()));
                    }

                    backgroundProperty().bind(row.backgroundProperty());
                }
            }
        });

        return listView;
    }

    private static class SysMLComponent {
        private final Node node;
        private final Association association;
        private final ASTNode sysNode;
        private final boolean isClosingBrace;
        private final int depth;

        private final BooleanProperty highlighted = new SimpleBooleanProperty(false);
        private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();
        private final ObjectProperty<Background> background = new SimpleObjectProperty<>();

        SysMLComponent(Node node, Color initialColor, boolean isClosingBrace, int depth) {
            this.node = node;
            this.association = AssociationColors.associationOf(node);
            this.isClosingBrace = isClosingBrace;
            this.depth = depth;

            this.sysNode = (node.getArtifact() != null &&
                    node.getArtifact().getData() instanceof ASTNodeArtifactData nodeData)
                    ? deserialize(nodeData.data()) : null;

            highlighted.addListener((o, oldVal, newVal) -> updateBackground());
            backgroundColor.addListener((o, oldVal, newVal) -> updateBackground());
            backgroundColor.set(initialColor);
        }

        private void updateBackground() {
            Color color = Boolean.TRUE.equals(highlighted.get()) ? Color.YELLOW : backgroundColor.get();
            if (color == null || Color.TRANSPARENT.equals(color)) {
                background.set(null);
            } else {
                background.set(new Background(new BackgroundFill(color, null, null)));
            }
        }

        Node getNode() {
            return node;
        }

        Association getAssociation() {
            return association;
        }

        ASTNode getSysNode() {
            return sysNode;
        }

        boolean isClosingBrace() {
            return isClosingBrace;
        }

        int getDepth() {
            return depth;
        }

        void setHighlighted(boolean flag) {
            highlighted.set(flag);
        }

        ObjectProperty<Color> backgroundColor() {
            return backgroundColor;
        }

        ReadOnlyObjectProperty<Background> backgroundProperty() {
            return background;
        }
    }
}