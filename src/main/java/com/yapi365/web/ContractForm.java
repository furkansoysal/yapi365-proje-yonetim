package com.yapi365.web;

import com.yapi365.domain.Contract;
import com.yapi365.domain.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Form nesnesi: entity doğrudan request'e bağlanmaz (mass-assignment koruması). */
public class ContractForm {

    private Long id;
    @NotNull(message = "Tür seçin")
    private ContractType type;
    @NotBlank(message = "Sözleşme no zorunlu")
    private String contractNo;
    @NotBlank(message = "Karşı taraf zorunlu")
    private String counterparty;
    @NotBlank(message = "Konu zorunlu")
    private String title;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    @PositiveOrZero
    private BigDecimal amount;
    private String currency = "TRY";
    @PositiveOrZero
    private BigDecimal guaranteeAmount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate guaranteeExpiry;

    public void applyTo(Contract c) {
        c.setType(type);
        c.setContractNo(contractNo.trim());
        c.setCounterparty(counterparty.trim());
        c.setTitle(title.trim());
        c.setStartDate(startDate);
        c.setEndDate(endDate);
        c.setAmount(amount);
        c.setCurrency(currency == null || currency.isBlank() ? "TRY" : currency);
        c.setGuaranteeAmount(guaranteeAmount);
        c.setGuaranteeExpiry(guaranteeExpiry);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
