package fr.skytasul.quests;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.jar.JarFile;

public class BeautyQuestsLoader implements PluginLoader {

	@Override
	public void classloader(PluginClasspathBuilder classpathBuilder) {
		List<String> librariesList;

		try (var pluginJar = new JarFile(classpathBuilder.getContext().getPluginSource().toFile());
				var pluginYamlReader = new InputStreamReader(pluginJar.getInputStream(pluginJar.getEntry("plugin.yml")))) {
			var config = YamlConfiguration.loadConfiguration(pluginYamlReader);
			librariesList = config.getStringList("libraries");
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to load libraries list", ex);
		}

		var resolver = new MavenLibraryResolver();
		resolver.addRepository(
				new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
		for (String library : librariesList) {
			resolver.addDependency(new Dependency(new DefaultArtifact(library), null));
		}

		classpathBuilder.addLibrary(resolver);
	}

}