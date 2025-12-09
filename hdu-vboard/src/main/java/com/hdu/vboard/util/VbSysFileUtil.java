package com.hdu.vboard.util;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class VbSysFileUtil {
  public static void saveFile(MultipartFile multipartFile, String fullPath) throws IOException {
    if (multipartFile.isEmpty()) return;
    File file = new File(fullPath);
    if (!file.getParentFile().exists()) {
      if (!file.getParentFile().mkdirs()) {
        throw new FileExistsException("创建文件夹出错");
      }
    }
    multipartFile.transferTo(file);
  }

  // vboard工作区根目录
  public static String getRootBasePath() {
    String absolutePath = FileUtil.getAbsolutePath(".");
    log.debug("absolutePath:{}", absolutePath);
    // 如果在jar包内
    if (absolutePath.contains("jar")) {
      String[] split = absolutePath.split("[^/]+\\.jar!");
      log.debug("jar包路径:{}", split[0]);
      String absoluteTempPath = split[0];
      if (absoluteTempPath.contains("target/")) {
        return FileUtil.getAbsolutePath(absoluteTempPath + "../");
      }else{
        return FileUtil.getAbsolutePath(absoluteTempPath);
      }
    }
    // 如果只是在target内
    if (absolutePath.contains("target/")) {
      log.debug("return path:{}", FileUtil.getAbsolutePath("../../"));
      return FileUtil.getAbsolutePath("../../");
    }
    return absolutePath;
  }

  public static String getVbBasePath() {
    return "vb/";
  }

  // 查找目录而非文件路径
  // vb
  // |__save      // namespace下保存top.v和bind.json
  // |__workbench // namespace下保存工作区
  public static String getSavePath(String dirName) {
    String basePath = getRootBasePath()+getVbBasePath();
    if (Objects.equals(dirName, "")) {
      basePath += "save";
    } else {
      basePath += "save/" + dirName;
    }
    String absolutePath = FileUtil.getAbsolutePath(basePath);
    if (absolutePath.contains("target/")) {
      return "../../" + basePath;
    }
    return basePath;
  }

  public static String getWorkbenchPath(String dirName) {
    String basePath = getRootBasePath()+getVbBasePath();
    if (Objects.equals(dirName, "")) {
      basePath += "workbench";
    } else {
      basePath += "workbench/" + dirName;
    }
    String absolutePath = FileUtil.getAbsolutePath(basePath);
    if (absolutePath.contains("target/")) {
      return "../../" + basePath;
    }
    return basePath;
  }

  public static String getFullSavePath(String dirName) {
    return FileUtil.getAbsolutePath(getSavePath(dirName));
  }

  public static String getFullWorkbenchPath(String dirName) {
    return FileUtil.getAbsolutePath(getWorkbenchPath(dirName));
  }

  public static void deleteDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          FileUtil.del(file);
        }
      }
    }
    FileUtil.del(directory);
  }
}
