package pl.robakowski.transactions;

import com.dslplatform.json.JsonWriter;

import java.math.BigDecimal;

public class Amount {

    long l;
    BigDecimal bd;

    public Amount(Amount amount) {
        if (amount != null) {
            l = amount.l;
            bd = amount.bd;
        }
    }

    public Amount(long l, BigDecimal bd) {
        this.l = l;
        this.bd = bd;
    }

    public void add(Amount that) {
        if (bd == null && that.bd == null) {
            l += that.l;
            return;
        }
        if (bd == null) {
            bd = BigDecimal.valueOf(l, 2);
        }
        bd = bd.add(that.bd != null ? that.bd : BigDecimal.valueOf(l, 2));
    }

    public void sub(Amount that) {
        if (bd == null && that.bd == null) {
            l -= that.l;
            return;
        }
        if (bd == null) {
            bd = BigDecimal.valueOf(l, 2);
        }
        bd = bd.subtract(that.bd != null ? that.bd : BigDecimal.valueOf(l, 2));
    }

    @Override
    public String toString() {
        JsonWriter writer = new JsonWriter();
        AmountConverter.JSON_WRITER.write(writer, this);
        return writer.toString();
    }

}
