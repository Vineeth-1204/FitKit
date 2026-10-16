package com.fitkit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyHistoryItemDto {
    private LocalDate date;
    private String dateStr;
    private String dayLabel; // Mon, Tue, etc.
    private BigDecimal totalCalories = BigDecimal.ZERO;
    private BigDecimal totalProteinG = BigDecimal.ZERO;
    private BigDecimal totalCarbsG = BigDecimal.ZERO;
    private BigDecimal totalFatG = BigDecimal.ZERO;
    private BigDecimal totalFiberG = BigDecimal.ZERO;
    private boolean hasEntries = false;

    public DailyHistoryItemDto() {}

    public DailyHistoryItemDto(LocalDate date, String dateStr, String dayLabel) {
        this.date = date;
        this.dateStr = dateStr;
        this.dayLabel = dayLabel;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDateStr() { return dateStr; }
    public void setDateStr(String dateStr) { this.dateStr = dateStr; }

    public String getDayLabel() { return dayLabel; }
    public void setDayLabel(String dayLabel) { this.dayLabel = dayLabel; }

    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }

    public BigDecimal getTotalProteinG() { return totalProteinG; }
    public void setTotalProteinG(BigDecimal totalProteinG) { this.totalProteinG = totalProteinG; }

    public BigDecimal getTotalCarbsG() { return totalCarbsG; }
    public void setTotalCarbsG(BigDecimal totalCarbsG) { this.totalCarbsG = totalCarbsG; }

    public BigDecimal getTotalFatG() { return totalFatG; }
    public void setTotalFatG(BigDecimal totalFatG) { this.totalFatG = totalFatG; }

    public BigDecimal getTotalFiberG() { return totalFiberG; }
    public void setTotalFiberG(BigDecimal totalFiberG) { this.totalFiberG = totalFiberG; }

    public boolean isHasEntries() { return hasEntries; }
    public void setHasEntries(boolean hasEntries) { this.hasEntries = hasEntries; }
}
