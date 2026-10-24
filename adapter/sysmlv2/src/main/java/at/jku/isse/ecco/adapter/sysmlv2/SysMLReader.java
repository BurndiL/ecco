package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.EccoException;
import at.jku.isse.ecco.adapter.ArtifactReader;
import at.jku.isse.ecco.adapter.dispatch.PluginArtifactData;
import at.jku.isse.ecco.artifact.Artifact;
import at.jku.isse.ecco.dao.EntityFactory;
import at.jku.isse.ecco.service.listener.ReadListener;
import at.jku.isse.ecco.tree.Node;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.monticore.lang.sysmlv2.SysMLv2Mill;
import de.monticore.lang.sysmlv2._ast.ASTSysMLModel;
import de.monticore.lang.sysmlv2._parser.SysMLv2Parser;
import de.monticore.lang.sysmlv2._visitor.SysMLv2Traverser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class SysMLReader implements ArtifactReader<Path, Set<Node.Op>> {
    private final EntityFactory entityFactory;
    private static final Map<Integer, String[]> prioritizedPatterns = Map.of(1, new String[]{"**.sysml", "**.kerml"});

    private final Path repositoryDir;

    public SysMLReader(EntityFactory entityFactory) {
        this(entityFactory, null);
    }

    @Inject
    public SysMLReader(EntityFactory entityFactory, @Named("repositoryDir") Path repositoryDir) {
        checkNotNull(entityFactory);

        this.entityFactory = entityFactory;
        this.repositoryDir = repositoryDir;
    }

    @Override
    public String getPluginId() {
        return SysMLPlugin.class.getName();
    }

    @Override
    public Map<Integer, String[]> getPrioritizedPatterns() {
        return prioritizedPatterns;
    }

    @Override
    public Set<Node.Op> read(Path base, Path[] input) {
        Set<Node.Op> nodes = new HashSet<>();

        for (Path path : input) {
            Path resolvedPath = base.resolve(path);
            SysMLv2Parser parser = SysMLv2Mill.parser();
            Optional<ASTSysMLModel> ast;
            Artifact.Op<PluginArtifactData> pluginArtifact = this.entityFactory.createArtifact(new PluginArtifactData(this.getPluginId(), path));
            Node.Op pluginNode = this.entityFactory.createOrderedNode(pluginArtifact);
            nodes.add(pluginNode);
            try {
                ast = parser.parse(resolvedPath.toString());

                if (ast.isEmpty()) {
                    throw new IOException("SysMLv2 Abstract Syntax Tree is empty");
                }

                generateEccoTree(ast.get(), pluginNode);

            } catch (IOException e) {
                // fail the commit rather than committing an empty file (see UnreadableFileCommitTest)
                throw new EccoException("Could not read file: " + resolvedPath, e);
            }


        }
        return nodes;
    }

    public void generateEccoTree(ASTSysMLModel tree, Node.Op node) {
        SysMLv2Traverser traverser = SysMLv2Mill.traverser();
        SysMLVisitor visitor = new SysMLVisitor(node, entityFactory);

        traverser.add4SysMLParts(visitor);

        tree.accept(traverser);
    }

    @Override
    public Set<Node.Op> read(Path[] input) {
        return this.read(Paths.get("."), input);
    }

    @Override
    public void addListener(ReadListener listener) {

    }

    @Override
    public void removeListener(ReadListener listener) {

    }
}
