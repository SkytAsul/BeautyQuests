package fr.skytasul.quests.api.utils;

import org.jetbrains.annotations.NotNull;
import java.util.stream.Stream;

public record Version(int major, int minor, int patch) implements Comparable<Version> {

	public static final Version ZERO = new Version(0, 0, 0);

	/**
	 * Checks if this version is the same as the version passed as parameters.
	 *
	 * @param major
	 * @param minor
	 * @param patch
	 * @return <code>true</code> if the current version is exactly the same as the passed version
	 */
	public boolean is(int major, int minor, int patch) {
		return major() == major && minor() == minor && patch() == patch;
	}

	/**
	 * Checks if this version is the same as the version passed as parameters.
	 *
	 * @param version
	 * @return <code>true</code> if the current version is exactly the same as the passed version
	 * @see Version#equals(Object)
	 */
	public boolean is(@NotNull Version version) {
		return this.equals(version);
	}

	/**
	 * Checks if this version is after or equal to the version passed as parameters.
	 *
	 * @param major
	 * @param minor
	 * @param patch
	 * @return <code>true</code> if the current version is after (inclusive) the passed version
	 */
	public boolean isAfter(int major, int minor, int patch) {
		if (major() > major)
			return true;
		if (major() < major)
			return false;

		if (minor() > minor)
			return true;
		if (minor() < minor)
			return false;

		return patch() >= patch;
	}

	/**
	 * Checks if this version is after or equal to the version passed as parameters.
	 *
	 * @param version
	 * @return <code>true</code> if the current version is after (inclusive) the passed version
	 */
	public boolean isAfter(@NotNull Version version) {
		return isAfter(version.major, version.minor, version.patch);
	}

	/**
	 * Checks if this version is strictly before the version passed as parameters.
	 *
	 * @param major
	 * @param minor
	 * @param patch
	 * @return <code>true</code> if the current version is before (exclusive) the passed version
	 */
	public boolean isBefore(int major, int minor, int patch) {
		return !isAfter(major, minor, patch);
	}

	/**
	 * Checks if this version is strictly before the version passed as parameters.
	 *
	 * @param version
	 * @return <code>true</code> if the current version is before (exclusive) the passed version
	 */
	public boolean isBefore(@NotNull Version version) {
		return isBefore(version.major, version.minor, version.patch);
	}

	@Override
	public int compareTo(Version o) {
		if (o.equals(this))
			return 0;
		return isAfter(o) ? 1 : -1;
	}

	@Override
	public final @NotNull String toString() {
		return toString(false);
	}

	public final @NotNull String toString(boolean omitPatch) {
		if (omitPatch && patch == 0)
			return "%d.%d".formatted(major, minor);
		return "%d.%d.%d".formatted(major, minor, patch);
	}

	public static @NotNull Version parse(@NotNull String string) throws IllegalArgumentException {
		var parts = string.split("\\.");
		if (parts.length < 2 || parts.length > 3)
			throw new IllegalArgumentException("Malformed version: " + string);

		int major = Integer.parseInt(parts[0]);
		int minor = Integer.parseInt(parts[1]);
		int patch = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;

		return new Version(major, minor, patch);
	}

	public static @NotNull Version @NotNull [] parseArray(String... versions) {
		return Stream.of(versions).map(Version::parse).toArray(Version[]::new);
	}

}
