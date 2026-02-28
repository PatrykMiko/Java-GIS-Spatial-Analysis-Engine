package org.example;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Fluent API for querying Admin Units (Builder Pattern).
 * Mimics SQL-like syntax in Java (selectFrom -> where -> and -> sort -> limit -> execute).
 */
public class AdminUnitQuery {
    AdminUnitList src;
    Predicate<AdminUnit> p = a -> true;
    Comparator<AdminUnit> cmp;
    int limit = Integer.MAX_VALUE;
    int offset = 0;

    AdminUnitQuery selectFrom(AdminUnitList src){
        this.src = src;
        return this;
    }

    AdminUnitQuery where(Predicate<AdminUnit> pred){
        this.p = pred;
        return this;
    }

    AdminUnitQuery and(Predicate<AdminUnit> pred){
        this.p = this.p.and(pred); // Method chaining predicate
        return this;
    }

    AdminUnitQuery or(Predicate<AdminUnit> pred){
        this.p = this.p.or(pred);
        return this;
    }

    AdminUnitQuery sort(Comparator<AdminUnit> cmp){
        this.cmp = cmp;
        return this;
    }

    AdminUnitQuery limit(int limit){
        this.limit = limit;
        return this;
    }

    AdminUnitQuery offset(int offset){
        this.offset = offset;
        return this;
    }

    /**
     * Triggers the evaluation of the query pipeline.
     */
    AdminUnitList execute(){
        if (src == null || src.units == null) {
            return new AdminUnitList();
        }
        AdminUnitList result = src;
        if (p != null) {
            result = result.filter(p, offset, limit);
        }
        if (cmp != null) {
            result = result.sort(cmp);
        }
        return result;
    }
}
