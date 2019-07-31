package grondag.fluidity.test;

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.fluids.v1.volume.fraction.MutableFraction;

class FractionTest {

    @Test
    void test() {

        MutableFraction f = MutableFraction.of(10);
        
        assert f.whole() == 10;
        assert f.numerator() == 0;
        assert f.divisor() == 1;
        
        f.subtract(4, 3);
        
        System.out.println(f.toString());
        
        f.subtract(7175, 1000);
        
        System.out.println(f.toString());
        
        f.add(4, 3);
        
        System.out.println(f.toString());
        
        f.add(7175, 1000);
        
        System.out.println(f.toString());
        
        f.subtract(10);
        
        System.out.println(f.toString());
        
        assert f.isZero();
    }

}
