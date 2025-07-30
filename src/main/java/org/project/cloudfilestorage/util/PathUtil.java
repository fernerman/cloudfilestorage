package org.project.cloudfilestorage.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.project.cloudfilestorage.exception.InvalidPathException;

@UtilityClass
public class PathUtil {

  public static final String USER_ROOT_DIR_PATTERN = "user-%s-files";
  private static final Pattern DIRECTORY_PATTERN = Pattern.compile("^[^\\\\/:*?\"<>|]+$");

  public static String getUserRootDirectory(int userId) {
    return USER_ROOT_DIR_PATTERN.formatted(Integer.toString(userId));
  }

  public static List<String> getVirtualRecursivePath(String objectPath) {
    List<String> result = new ArrayList<>();

    String[] parts = objectPath.split("/");
    StringBuilder pathBuilder = new StringBuilder();
    for (int i = 0; i < parts.length ; i++) {
      pathBuilder.append(parts[i]).append("/");
      result.add(pathBuilder.toString());
    }
    return result;
  }


  public String getAbsolutePath(int userId, String relativePath) {
    String userRootPath = getUserRootDirectory(userId);
    String cleanedRelativePath = cleanRelativePath(relativePath);
    if (cleanedRelativePath.isBlank()) {
      return userRootPath + "/";
    }
    String resourceName = getResourceNameFromPath(relativePath);
    validateName(resourceName);

    if (isDirectory(relativePath)) {
      return userRootPath + "/" + cleanedRelativePath + "/";
    }
    return userRootPath + "/" + cleanedRelativePath;
  }

  public static String getParentDirectory(int id, String path) {
    String absolutePath = getAbsolutePath(id, path);
    return trimmed(absolutePath);
  }

  public static String getFolderPath(String relativePath) {
    String trimmed = trimmed(relativePath);
    int lastSlash = trimmed.lastIndexOf('/');
    if (lastSlash == -1) {
      return "/";
    }
    return trimmed.substring(0, lastSlash + 1);
  }

  public static String getResourceNameFromPath(String relativePath) {
    if (relativePath == null || relativePath.isEmpty()) {
      return "";
    }
    String[] parts = relativePath.split("/");
    return parts.length > 0 ? parts[parts.length - 1] : "";

  }

  public static String getRelativePath(String fullPath, int userId) {
    String userRoot = getUserRootDirectory(userId);
    return subtractBasePath(fullPath, userRoot);
  }

  public static String subtractBasePath(String fullPath, String baseFolder) {
    if (fullPath == null || baseFolder == null) {
      return fullPath;
    }
    String base = baseFolder.endsWith("/") ? baseFolder : baseFolder + "/";
    String path = fullPath.startsWith("/") ? fullPath.substring(1) : fullPath;

    if (path.startsWith(base)) {
      return path.substring(base.length());
    }
    if (path.equals(base.substring(0, base.length() - 1))) {
      return "";
    }
    return path;
  }

  public static boolean isDirectory(String path) {
    return path != null && path.endsWith("/");
  }

  public static boolean matchesQuery(String path, String query) {
    if (path == null || query == null) {
      return false;
    }
    return query.contains(path);
  }

  public static String getLastSegmentWithoutExtension(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }

    String cleaned = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

    int lastSlash = cleaned.lastIndexOf('/');
    String lastSegment = lastSlash >= 0 ? cleaned.substring(lastSlash + 1) : cleaned;

    int dotIndex = lastSegment.lastIndexOf('.');
    if (dotIndex > 0) {
      return lastSegment.substring(0, dotIndex);
    }

    return lastSegment;
  }


  public static void validateName(String path) {
    if (path == null) {
      throw new InvalidPathException("Путь не должен быть пустым");
    }
    if (path.contains("..")) {
      throw new InvalidPathException("Путь не должен содержать '..'");
    }
    if (!DIRECTORY_PATTERN.matcher(path).matches()) {
      throw new InvalidPathException(
          String.format("Путь %s содержит недопустимые символы: \\ / : * ? \" < > |", path));
    }
    if (path.length() > 255) {
      throw new InvalidPathException("Путь не должен быть больше 255 символов. ");
    }
  }

  private String trimmed(String path) {
    String trimmedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    return trimmedPath.substring(0, trimmedPath.lastIndexOf("/") + 1);
  }

  private static String cleanRelativePath(String relativePath) {
    return relativePath.strip()
        .replaceAll("^/+", "")
        .replaceAll("/+$", "");
  }
}
