package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.adapter.*;
import at.jku.isse.ecco.adapter.sysmlv2.viewer.SysMLCodeViewer;
import at.jku.isse.ecco.adapter.sysmlv2.viewer.SysMLSVGViewer;
import at.jku.isse.ecco.tree.Node;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import de.monticore.lang.sysmlv2.SysMLv2Mill;

import java.nio.file.Path;
import java.util.Set;

public class SysMLModule extends AbstractModule {

    public final static Object sysMillLock = new Object();

    @Override
    protected void configure() {
        SysMLv2Mill.init();

        final Multibinder<ArtifactReader<Path, Set<Node.Op>>> readerMultibinder = Multibinder.newSetBinder(binder(),
                new TypeLiteral<>() {
                });
        readerMultibinder.addBinding().to(SysMLReader.class);

        final Multibinder<ArtifactWriter<Set<Node>, Path>> writerMultibinder = Multibinder.newSetBinder(binder(),
                new TypeLiteral<>() {
                });
        writerMultibinder.addBinding().to(SysMLFileWriter.class);

        // no JavaFX viewers without a GUI (the command line and the REST server set ecco.headless)
        if (!Boolean.getBoolean("ecco.headless")) {
            final Multibinder<AssociationInfoArtifactViewer> assInfoMultiBinder = Multibinder.newSetBinder(binder(),
                    new TypeLiteral<>() {});
            assInfoMultiBinder.addBinding().to(SysMLCodeViewer.class);

            final Multibinder<ArtifactViewer> viewerMultibinder = Multibinder.newSetBinder(binder(),
                    new TypeLiteral<>() {
                    });
            viewerMultibinder.addBinding().to(SysMLSVGViewer.class);
        }

        final Multibinder<ArtifactExporter<Set<Node>, Path>> exporterMultibinder = Multibinder.newSetBinder(binder(),
                new TypeLiteral<>() {
                });
        exporterMultibinder.addBinding().to(SysMLExporter.class);
    }
}
