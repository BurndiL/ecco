package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.artifact.ArtifactData;
import de.monticore.ast.ASTNode;
import de.monticore.lang.sysmlparts._ast.*;

import java.util.Objects;

public record ASTNodeArtifactData(String name, String data) implements ArtifactData {
    private static final long serialVersionUID = -5846113510938625687L;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
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
        if (Objects.equals(myNode, thatNode)) return true;

        //TODO: make this more accurate
        var myData = switch (myNode){
            case ASTPartDef a -> a.getName();
            case ASTPartUsage a -> a.getName();
            case ASTSysMLPackage a -> a.getName();
            case ASTAttributeDef a -> a.getName();
            case ASTAttributeUsage a -> a.getName();
            default -> "this";
        };

        var thatData = switch (thatNode){
            case ASTPartDef a -> a.getName();
            case ASTPartUsage a -> a.getName();
            case ASTSysMLPackage a -> a.getName();
            case ASTAttributeDef a -> a.getName();
            case ASTAttributeUsage a -> a.getName();
            default -> "that";
        };

        return Objects.equals(myData, thatData);
    }
}
