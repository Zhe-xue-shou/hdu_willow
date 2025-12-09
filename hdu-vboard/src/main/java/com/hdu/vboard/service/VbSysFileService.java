package com.hdu.vboard.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface VbSysFileService {
  void clearWorkbenchFile(HttpServletRequest request);

  String saveVerilogFile(HttpServletRequest request, MultipartFile verilogFile) throws IOException;

  String saveBindFile(HttpServletRequest request, MultipartFile bindFile) throws IOException;
}
