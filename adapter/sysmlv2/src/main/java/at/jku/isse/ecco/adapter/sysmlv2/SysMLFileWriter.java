package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.adapter.ArtifactWriter;
import at.jku.isse.ecco.adapter.sysmlv2.viewer.SysMLCodeViewer;
import at.jku.isse.ecco.service.listener.WriteListener;
import at.jku.isse.ecco.tree.Node;
import de.monticore.ast.ASTNode;
import de.monticore.lang.sysmlv2.SysMLv2Mill;

import java.nio.file.Path;
import java.util.Set;

import static at.jku.isse.ecco.adapter.sysmlv2.ASTSerializer.deserialize;

public class SysMLFileWriter implements ArtifactWriter<Set<Node>, Path> {
    @Override
    public String getPluginId() {
        return "";
    }

    @Override
    public Path[] write(Path base, Set<Node> input) {
        return new Path[0];
    }

    @Override
    public Path[] write(Set<Node> input) {
        return new Path[0];
    }

    @Override
    public void addListener(WriteListener listener) {

    }

    @Override
    public void removeListener(WriteListener listener) {

    }

    public static String getSysML(Node node) {
        return flattenTree(node, 0);
    }

    private static String flattenTree(Node node, int depth) {
        StringBuilder sb = new StringBuilder();
        String lastPrinted = "";
        for (Node child : node.getChildren()) {
            String indent = "    ".repeat(Math.max(0, depth));
            ASTNode astNode = (child.getArtifact() != null &&
                    child.getArtifact().getData() instanceof ASTNodeArtifactData nodeData)
                    ? deserialize(nodeData.data()) : null;

            if (astNode != null) {
                if (astNode.getClass().getSimpleName().contains("SysMLModel")) {
                    sb.append(flattenTree(child, depth));
                    continue;
                }

                String printed = SysMLv2Mill.prettyPrint(astNode, true).trim();

                printed = printed.split("\\r?\\n")[0].trim();

                // strip any trailing '{' or ';' because they fuck up the parsing
                printed = printed.replaceAll("[{;]+\\s*$", "").trim();

                if (printed.isEmpty() || printed.equals(lastPrinted)) {
                    sb.append(flattenTree(child, depth));
                    continue;
                }

                lastPrinted = printed;

                if (!child.getChildren().isEmpty()) {
                    sb.append(indent).append(printed).append(" {\n");
                    sb.append(flattenTree(child, depth + 1));
                    sb.append(indent).append("}\n");
                } else {
                    sb.append(indent).append(printed).append(";\n");
                }
            } else {
                // Non-AST structural nodes
                sb.append(flattenTree(child, depth));
            }
        }
        return sb.toString();
    }
}
