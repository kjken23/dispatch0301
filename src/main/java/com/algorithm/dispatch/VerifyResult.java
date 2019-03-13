package com.algorithm.dispatch;

import java.math.BigDecimal;

/**
 * @author kj
 */
public class VerifyResult {
    private BigDecimal reliability;
    private BigDecimal repetitiveRate;

    public VerifyResult() {

    }

    public VerifyResult(BigDecimal reliability, BigDecimal repetitiveRate) {
        this.reliability = reliability;
        this.repetitiveRate = repetitiveRate;
    }

    public BigDecimal getReliability() {
        return reliability;
    }

    public void setReliability(BigDecimal reliability) {
        this.reliability = reliability;
    }

    public BigDecimal getRepetitiveRate() {
        return repetitiveRate;
    }

    public void setRepetitiveRate(BigDecimal repetitiveRate) {
        this.repetitiveRate = repetitiveRate;
    }

    @Override
    public String toString() {
        return "VerifyResult{" +
                "reliability=" + reliability +
                ", repetitiveRate=" + repetitiveRate +
                '}';
    }
}
