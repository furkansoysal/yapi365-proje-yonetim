package com.yapi365;

import com.yapi365.repo.ContractRepository;
import com.yapi365.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ContractRepository contracts;

    @Autowired
    AuditService audit;

    @Test
    void girisSayfasiAcilir() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void girisYapmayanYonlendirilir() throws Exception {
        mvc.perform(get("/contracts")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails("proje.muduru@demo.local")
    void projeMuduruPaneliVeSozlesmeleriGorur() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/contracts")).andExpect(status().isOk());
        mvc.perform(get("/documents")).andExpect(status().isOk());
        mvc.perform(get("/mail")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("izleyici@demo.local")
    void izleyiciYonetimEkraninaGiremez() throws Exception {
        mvc.perform(get("/admin/roles")).andExpect(status().isForbidden());
        mvc.perform(get("/contracts/new")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("yonetici@demo.local")
    void yoneticiYetkiMatrisiniGorur() throws Exception {
        mvc.perform(get("/admin/roles")).andExpect(status().isOk());
        mvc.perform(get("/admin/audit")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("proje.muduru@demo.local")
    void sozlesmeDegisikligiGecmiseYazilir() throws Exception {
        var c = contracts.findAll().stream().filter(x -> x.getContractNo().equals("SZL-021")).findFirst().orElseThrow();
        mvc.perform(post("/contracts/save").with(csrf())
                        .param("id", c.getId().toString())
                        .param("type", "TEDARIK")
                        .param("contractNo", "SZL-021")
                        .param("counterparty", "Marmara Hazır Beton A.Ş.")
                        .param("title", "C30/C35 hazır beton tedariki")
                        .param("amount", "4100000")
                        .param("currency", "TRY"))
                .andExpect(status().is3xxRedirection());

        var history = audit.contractHistory(c.getId());
        assertThat(history).hasSizeGreaterThanOrEqualTo(2);
        assertThat(history.get(0).user()).isEqualTo("proje.muduru@demo.local");
        assertThat(history.get(0).changes()).extracting(AuditService.Change::field).contains("Konu", "Tutar");
        mvc.perform(get("/contracts/" + c.getId() + "/history")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("santiye.sefi@demo.local")
    void dosyaYuklemeVersiyonOlusturur() throws Exception {
        var file = new MockMultipartFile("file", "test.pdf", "application/pdf", "deneme".getBytes());
        mvc.perform(multipart("/documents").file(file).with(csrf())
                        .param("folder", "Test")
                        .param("title", "Test Dokümanı"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(multipart("/documents").file(file).with(csrf())
                        .param("folder", "Test")
                        .param("title", "Test Dokümanı"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("message", "Yüklendi: test.pdf (v2)"));
    }
}
