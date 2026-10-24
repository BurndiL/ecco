package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.adapter.ArtifactPlugin;
import com.google.inject.Module;

public class SysMLPlugin extends ArtifactPlugin {

    private static final String[] fileTypes = new String[]{"sysml", "kerml"};

    private final SysMLModule module = new SysMLModule();

    public String[] getFileTypes() {
        return fileTypes;
    }

    @Override
    public String getPluginId() {
        return SysMLPlugin.class.getName();
    }

    @Override
    public Module getModule() {
        return this.module;
    }

    @Override
    public String getName() {
        return "SysMLv2ArtifactPlugin";
    }

    @Override
    public String getDescription() {
        return "SysMLv2 Artifact Plugin";
    }
}
