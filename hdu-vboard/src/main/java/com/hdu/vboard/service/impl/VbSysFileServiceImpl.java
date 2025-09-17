package com.hdu.vboard.service.impl;

import cn.hutool.core.io.FileUtil;
import com.hdu.vboard.exception.InvalidFileSuffixException;
import com.hdu.vboard.service.VbSysFileService;
import com.hdu.vboard.util.VbSysFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class VbSysFileServiceImpl implements VbSysFileService {
  public void clearWorkbenchFile(HttpServletRequest request) {
    String token = request.getHeader("token");
    String folderPath = VbSysFileUtil.getFullSavePath(token);

    File folder = new File(folderPath);
    if (folder.exists() && folder.isDirectory()) {
      File[] files = folder.listFiles();
      if (files != null) {
        for (File file : files) {
          FileUtil.del(file);
        }
      }
      log.debug("Cleared all files in save folder: {}", folderPath);
    } else {
      log.debug("save folder does not exist: {}", folderPath);
    }
  }

  public String saveVerilogFile(HttpServletRequest request, MultipartFile verilogFile) throws IOException {
    String originalFileName = verilogFile.getOriginalFilename();
    if (originalFileName == null) {
      throw new IOException("文件为空");
    }
    String verilogPattern = ".*?\\.v$";
    // 进行文件名校验，确认上传的是后缀为v的文件
    if (originalFileName.matches(verilogPattern)) {
      String token = request.getHeader("token");
      String filePath = VbSysFileUtil.getFullSavePath(token) + "/" + originalFileName;
      VbSysFileUtil.saveFile(verilogFile, filePath);
      log.debug("Verilog file {} saved to {} successfully!", originalFileName, filePath);
      return filePath;
    } else {
      throw new InvalidFileSuffixException("文件后缀不为.v");
    }
  }

  public String saveBindFile(HttpServletRequest request, MultipartFile bindFile) throws IOException {
    String originalFileName = bindFile.getOriginalFilename();
    if (originalFileName == null) {
      throw new IOException("文件为空");
    }
    String verilogPattern = ".*?\\.json$";
    // 进行文件名校验，确认上传的是后缀为v的文件
    if (originalFileName.matches(verilogPattern)) {
      String token = request.getHeader("token");
      String filePath = VbSysFileUtil.getFullSavePath(token) + "/" + originalFileName;
      VbSysFileUtil.saveFile(bindFile, filePath);
      log.debug("Bind json saved to {} successfully!", filePath);
      return filePath;
    } else {
      throw new InvalidFileSuffixException("文件后缀不为.json");
    }
  }
}
