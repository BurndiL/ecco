package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.artifact.ArtifactData;
import de.monticore.ast.ASTNode;
import de.monticore.lang.sysmlparts._ast.*;

import java.util.Objects;
import java.util.Set;

public record ASTNodeArtifactData(String name, String data) implements ArtifactData {
    private static final long serialVersionUID = -5846113510938625687L;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        //TODO: make more accurate
        ASTNode myNode = ASTSerializer.deserialize(data);
        return Objects.hash(myNode.getClass(), name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASTNodeArtifactData that = (ASTNodeArtifactData) o;
        if (Objects.equals(data, that.data)) return true;
        ASTNode myNode = ASTSerializer.deserialize(data);
        ASTNode thatNode = ASTSerializer.deserialize(that.data);
        if (myNode.getClass() != thatNode.getClass()) return false;
        if (Objects.equals(myNode, thatNode)) return true;

        //TODO: make this more accurate
        Set<Object> myData = switch (myNode){
            case ASTPartDef a -> Set.of(a.getName());
            case ASTPartUsage a -> Set.of(a.getName());
            case ASTSysMLPackage a -> Set.of(a.getName());
            case ASTAttributeDef a -> Set.of(a.getName());
            case ASTAttributeUsage a -> Set.of(a.getName());
            default -> Set.of("this");
        };

        Set<Object> thatData = switch (thatNode){
            case ASTPartDef a -> Set.of(a.getName());
            case ASTPartUsage a -> Set.of(a.getName());
            case ASTSysMLPackage a -> Set.of(a.getName());
            case ASTAttributeDef a -> Set.of(a.getName());
            case ASTAttributeUsage a -> Set.of(a.getName());
            default -> Set.of("that");
        };

        return Objects.equals(myData, thatData);
    }
}
