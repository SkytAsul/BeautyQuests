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
import java.util.logging.Logger;

public class BeautyQuestsLoader implements PluginLoader {

	private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";

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

		String repository;
		try {
			repository = MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR;
			if (repository.equals("https://maven-central.storage-download.googleapis.com/maven2")) {
				Logger.getGlobal().info("""
					Paper is using the Google mirror for Maven central repository, which lacks some artifacts.
					BeautyQuests will use the default Maven central CDN until a better mirror has been found.
					""");
				repository = MAVEN_CENTRAL;
			}
		} catch (NoSuchFieldError ex) {
			repository = MAVEN_CENTRAL;
		}
		resolver.addRepository(new RemoteRepository.Builder("central", "default", repository).build());

		for (String library : librariesList) {
			resolver.addDependency(new Dependency(new DefaultArtifact(library), null));
		}

		classpathBuilder.addLibrary(resolver);
	}

}