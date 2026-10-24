package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.artifact.Artifact;
import at.jku.isse.ecco.artifact.ArtifactData;
import at.jku.isse.ecco.dao.EntityFactory;
import at.jku.isse.ecco.tree.Node;
import de.monticore.ast.ASTNode;
import de.monticore.lang.sysmlconstraints._visitor.SysMLConstraintsVisitor2;
import de.monticore.lang.sysmlparts._ast.*;
import de.monticore.lang.sysmlparts._visitor.SysMLPartsVisitor2;

import static at.jku.isse.ecco.adapter.sysmlv2.ASTSerializer.serialize;

/**
 * This Visitor traverses the AST provided by the SysMLv2 Parser.
 * It's Depth First.
 **/
public class SysMLVisitor implements SysMLPartsVisitor2, SysMLConstraintsVisitor2 {

    private Node.Op curNode;
    private final EntityFactory entityFactory;

    public SysMLVisitor(Node.Op head, EntityFactory entityFactory) {
        this.curNode = head;
        this.entityFactory = entityFactory;
    }

    //region Parts
    @Override
    public void visit(ASTPartDef sysNode) {
        // Detects: "part def Car;"
        Artifact.Op<ArtifactData> artifact = entityFactory.createArtifact(
                new ASTNodeArtifactData("part def " + sysNode.getName(), serialize(sysNode)));

        Node.Op childNode = this.entityFactory.createNode(artifact);
        curNode.addChild(childNode);
        curNode = childNode;
    }

    @Override
    public void endVisit(ASTPartDef node) {
        curNode = curNode.getParent();
    }

    @Override
    public void visit(ASTPartUsage sysNode) {
        // Detects: "part myCar : Car;"
        Artifact.Op<ArtifactData> artifact = entityFactory.createArtifact(
                new ASTNodeArtifactData("part " + sysNode.getName(), serialize(sysNode)));

        Node.Op childNode = this.entityFactory.createNode(artifact);
        curNode.addChild(childNode);
        curNode = childNode;
    }

    @Override
    public void endVisit(ASTPartUsage node) {
        curNode = curNode.getParent();
    }
    //endregion

    //region Packages
    @Override
    public void visit(ASTSysMLPackage sysNode) {
        // Detects: "package VehicleModel"
        Artifact.Op<ArtifactData> artifact = entityFactory.createArtifact(
                new ASTNodeArtifactData("package " + sysNode.getName(), serialize(sysNode)));

        Node.Op childNode = this.entityFactory.createNode(artifact);
        curNode.addChild(childNode);
        curNode = childNode;
    }

    @Override
    public void endVisit(ASTSysMLPackage node) {
        curNode = curNode.getParent();
    }
    //endregion

    //region Attributes
    @Override
    public void visit(ASTAttributeDef sysNode) {
        // Detects: "attribute def Point3D"
        Artifact.Op<ArtifactData> artifact = entityFactory.createArtifact(
                new ASTNodeArtifactData("attribute def " + sysNode.getName(), serialize(sysNode)));

        Node.Op childNode = this.entityFactory.createNode(artifact);
        curNode.addChild(childNode);
        curNode = childNode;
    }

    @Override
    public void endVisit(ASTAttributeDef node) {
        curNode = curNode.getParent();
    }

    @Override
    public void visit(ASTAttributeUsage sysNode) {
        // Detects: "attribute batteryCapacity : ScalarValues::Real;"
        Artifact.Op<ArtifactData> artifact = entityFactory.createArtifact(
                new ASTNodeArtifactData("attribute " + sysNode.getName(), serialize(sysNode)));

        Node.Op childNode = this.entityFactory.createNode(artifact);
        curNode.addChild(childNode);
    }

    @Override
    public void endVisit(ASTAttributeUsage node) {
        curNode = curNode.getParent();
    }
    //endregion

}
