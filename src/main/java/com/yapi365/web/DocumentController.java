package com.yapi365.web;

import com.yapi365.domain.Document;
import com.yapi365.domain.DocumentVersion;
import com.yapi365.domain.Project;
import com.yapi365.repo.DocumentRepository;
import com.yapi365.repo.DocumentVersionRepository;
import com.yapi365.service.DocumentService;
import com.yapi365.service.FileStorageService;
import com.yapi365.service.ProjectAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final ProjectAccessService access;
    private final DocumentRepository documents;
    private final DocumentVersionRepository versions;
    private final DocumentService service;
    private final FileStorageService storage;

    public DocumentController(ProjectAccessService access, DocumentRepository documents,
                              DocumentVersionRepository versions, DocumentService service, FileStorageService storage) {
        this.access = access;
        this.documents = documents;
        this.versions = versions;
        this.service = service;
        this.storage = storage;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOKUMAN:VIEW')")
    @Transactional(readOnly = true)
    public String list(Authentication auth, HttpSession session, Model model) {
        Project p = access.current(auth, session);
        List<Document> docs = documents.findByProjectIdOrderByFolderPathAscTitleAsc(p.getId());
        Map<String, List<Document>> byFolder = docs.stream()
                .collect(Collectors.groupingBy(Document::getFolderPath, TreeMap::new, Collectors.toList()));
        model.addAttribute("folders", byFolder);
        return "documents";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOKUMAN:EDIT')")
    public String upload(@RequestParam String folder, @RequestParam String title, @RequestParam MultipartFile file,
                         Authentication auth, HttpSession session, RedirectAttributes ra) throws IOException {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Dosya seçilmedi");
            return "redirect:/documents";
        }
        Project p = access.current(auth, session);
        try (InputStream in = file.getInputStream()) {
            DocumentVersion v = service.upload(p, folder, title.isBlank() ? file.getOriginalFilename() : title,
                    file.getOriginalFilename(), file.getContentType(), in, auth.getName());
            ra.addFlashAttribute("message", "Yüklendi: " + v.getFileName() + " (v" + v.getVersionNo() + ")");
        }
        return "redirect:/documents";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOKUMAN:VIEW')")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Authentication auth, HttpSession session, Model model) {
        Document doc = owned(id, auth, session);
        model.addAttribute("doc", doc);
        model.addAttribute("versions", versions.findByDocumentIdOrderByVersionNoDesc(id));
        return "document-detail";
    }

    @GetMapping("/versions/{versionId}/download")
    @PreAuthorize("hasAuthority('DOKUMAN:VIEW')")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(@PathVariable Long versionId, Authentication auth, HttpSession session) {
        DocumentVersion v = versions.findById(versionId).orElseThrow();
        owned(v.getDocument().getId(), auth, session);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(v.getFileName(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(storage.load(v.getStoragePath()));
    }

    private Document owned(Long id, Authentication auth, HttpSession session) {
        Project p = access.current(auth, session);
        Document doc = documents.findById(id).orElseThrow();
        if (!doc.getProject().getId().equals(p.getId())) {
            throw new AccessDeniedException("Bu doküman seçili projeye ait değil");
        }
        return doc;
    }
}
