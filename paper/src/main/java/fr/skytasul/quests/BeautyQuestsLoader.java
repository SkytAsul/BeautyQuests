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

	private static final String MAVEN_CENTRAL = "https://repo.papermc.io/repository/maven-public/";

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
				classpathBuilder.getContext().getLogger().info("""
					Paper is using the Google mirror for Maven central repository, which lacks some artifacts.
					BeautyQuests will use PaperMC's public repository instead.
					""");
				repository = MAVEN_CENTRAL;
			}
		} catch (NoSuchFieldError ex) {
			repository = MAVEN_CENTRAL;
		}
		resolver.addRepository(new RemoteRepository.Builder("central", "default", repository).build());

		classpathBuilder.getContext().getLogger().info("Loading {} libraries using Paper plugin loader...",
				librariesList.size());
		for (String library : librariesList) {
			var dependency = new Dependency(new DefaultArtifact(library), null);

			// XXX: skipping Adventure does not seem to bring anything and might shoot us in the knee instead
			// if (library.startsWith("net.kyori:adventure-api:")) {
			// 	classpathBuilder.getContext().getLogger().debug("Skipping adventure on Paper");
			// 	continue;
			// }
            //
			// if (library.startsWith("net.kyori:adventure-platform-bukkit:")) {
			// 	classpathBuilder.getContext().getLogger().debug("Excluding adventure from adventure-platform-bukkit");
			// 	dependency = dependency.setExclusions(List.of(new Exclusion("net.kyori", "adventure-api", null, "jar")));
			// }

			resolver.addDependency(dependency);
		}

		classpathBuilder.addLibrary(resolver);
	}

}