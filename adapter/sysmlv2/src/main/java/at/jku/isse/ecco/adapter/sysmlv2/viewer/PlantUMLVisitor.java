package at.jku.isse.ecco.adapter.sysmlv2.viewer;

import at.jku.isse.ecco.EccoException;
import de.monticore.lang.sysmlparts._ast.*;
import de.monticore.lang.sysmlv2.SysMLv2Mill;
import de.monticore.lang.sysmlv2._ast.ASTSysMLModel;
import de.monticore.lang.sysmlv2._parser.SysMLv2Parser;
import de.monticore.lang.sysmlv2._visitor.SysMLv2Traverser;
import de.monticore.lang.sysmlv2._visitor.SysMLv2Visitor2;
import de.monticore.lang.sysmlparts._visitor.SysMLPartsVisitor2;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

// 1. Implement ALL required visitor interfaces
public class PlantUMLVisitor implements SysMLv2Visitor2, SysMLPartsVisitor2 {

    private final StringBuilder uml = new StringBuilder();
    private String indent = "";

    public PlantUMLVisitor() {
        uml.append("@startuml\n");
        uml.append("skinparam componentStyle rectangle\n");
    }

    public String getPlantUMLString() {
        return uml.toString() + "@enduml\n";
    }

    private void increaseIndent() { indent += "  "; }
    private void decreaseIndent() { indent = indent.substring(0, indent.length() - 2); }

    //region Packages
    @Override
    public void visit(ASTSysMLPackage sysNode) {
        uml.append(indent).append("package \"").append(sysNode.getName()).append("\" {\n");
        increaseIndent();
    }

    @Override
    public void endVisit(ASTSysMLPackage node) {
        decreaseIndent();
        uml.append(indent).append("}\n");
    }
    //endregion

    //region Parts
    @Override
    public void visit(ASTPartDef sysNode) {
        uml.append(indent).append("class \"").append(sysNode.getName()).append("\" <<part def>> {\n");
        increaseIndent();
    }

    @Override
    public void endVisit(ASTPartDef node) {
        decreaseIndent();
        uml.append(indent).append("}\n");
    }

    @Override
    public void visit(ASTPartUsage sysNode) {
        // PlantUML does not understand "part". Map it to an "object" with a stereotype instead.
        uml.append(indent).append("object \"").append(sysNode.getName()).append("\" <<part>>\n");
    }
    //endregion

    //region Attributes
    @Override
    public void visit(ASTAttributeDef sysNode) {
        uml.append(indent).append("class \"").append(sysNode.getName()).append("\" <<attribute def>> {\n");
        increaseIndent();
    }

    @Override
    public void endVisit(ASTAttributeDef node) {
        decreaseIndent();
        uml.append(indent).append("}\n");
    }

    @Override
    public void visit(ASTAttributeUsage sysNode) {
        uml.append(indent).append("object \"").append(sysNode.getName()).append("\" <<part>>\n");
    }
    //endregion

    /**
     * Parses SysML, runs the visitor, and uses the PlantUML library to return an SVG string.
     */
    public static String getSVG(String sysML) {
        SysMLv2Parser parser = SysMLv2Mill.parser();

        try {
            Optional<ASTSysMLModel> ast = parser.parse(new StringReader(sysML));
            if (ast.isEmpty()) {
                throw new IOException("SysMLv2 Abstract Syntax Tree is empty");
            }

            SysMLv2Traverser traverser = SysMLv2Mill.traverser();
            PlantUMLVisitor visitor = new PlantUMLVisitor();

            //traverser.add4SysMLv2(visitor);
            traverser.add4SysMLParts(visitor);

            ast.get().accept(traverser);

            String plantUmlSource = visitor.getPlantUMLString();

            SourceStringReader reader = new SourceStringReader(plantUmlSource);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            reader.outputImage(os, new FileFormatOption(FileFormat.SVG));

            return os.toString(StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new EccoException("Could not parse: " + sysML, e);
        }
    }
}