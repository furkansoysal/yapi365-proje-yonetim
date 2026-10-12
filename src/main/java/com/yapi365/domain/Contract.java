package com.yapi365.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "contract")
@Audited
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Project project;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractType type;

    @NotBlank
    @Column(name = "contract_no", nullable = false, length = 50)
    private String contractNo;

    @NotBlank
    @Column(nullable = false)
    private String counterparty;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @PositiveOrZero
    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "TRY";

    @PositiveOrZero
    @Column(name = "guarantee_amount", precision = 18, scale = 2)
    private BigDecimal guaranteeAmount;

    @Column(name = "guarantee_expiry")
    private LocalDate guaranteeExpiry;

    public Contract() {
    }

    /** Değişiklik geçmişinde alan bazlı karşılaştırma için. */
    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Tür", type == null ? null : type.getLabel());
        m.put("Sözleşme No", contractNo);
        m.put("Karşı Taraf", counterparty);
        m.put("Konu", title);
        m.put("Başlangıç", startDate);
        m.put("Bitiş", endDate);
        m.put("Tutar", amount == null ? null : amount.stripTrailingZeros().toPlainString() + " " + currency);
        m.put("Teminat", guaranteeAmount == null ? null : guaranteeAmount.stripTrailingZeros().toPlainString());
        m.put("Teminat Bitişi", guaranteeExpiry);
        return m;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public ContractType getType() { return type; }
    public void setType(ContractType type) { this.type = type; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getCounterparty() { return counterparty; }
    public void setCounterparty(String counterparty) { this.counterparty = counterparty; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getGuaranteeAmount() { return guaranteeAmount; }
    public void setGuaranteeAmount(BigDecimal guaranteeAmount) { this.guaranteeAmount = guaranteeAmount; }
    public LocalDate getGuaranteeExpiry() { return guaranteeExpiry; }
    public void setGuaranteeExpiry(LocalDate guaranteeExpiry) { this.guaranteeExpiry = guaranteeExpiry; }
}
