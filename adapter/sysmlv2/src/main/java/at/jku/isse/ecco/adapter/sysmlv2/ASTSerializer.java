package at.jku.isse.ecco.adapter.sysmlv2;

import com.thoughtworks.xstream.XStream;
import de.monticore.ast.ASTNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Turns an ASTNode into XML format, so it can be stored in the repositories.
 * Also deserializes Strings back into ASTNodes.
 */
public class ASTSerializer {

    private static final XStream xstream = new XStream();

    static {
        xstream.allowTypesByWildcard(new String[] {
                "de.monticore.**",
                "at.jku.isse.ecco.**",
                "de.se_rwth.commons.**",
        });

        // For circular parent/child references
        xstream.setMode(XStream.ID_REFERENCES);
    }

    /**
     * Serializes ANY ASTNode to an XML String
     */
    public static String serialize(ASTNode node) {
        if (node == null) return null;
        return xstream.toXML(node);
    }

    /**
     * Deserializes the XML String back into an ASTNode
     */
    public static ASTNode deserialize(String xmlData) {
        if (xmlData == null) return null;
        return (ASTNode) xstream.fromXML(xmlData);
    }
}