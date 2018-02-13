package uk.co.wedgetech.geodesy

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

class DoubleUtilsKtTest {

    @Test
    fun toFixed() {
            Assert.assertEquals(0.0, 0.01.toFixed(1), 0.0001)
            Assert.assertEquals(0.1, 0.07.toFixed(1), 0.0001)
            Assert.assertEquals(4238764872.75, 4238764872.745398675983467.toFixed(2), 0.000001)
            Assert.assertEquals(4238764872.74539868, 4238764872.745398675983467.toFixed(8), 0.0000000001)
            Assert.assertEquals(4238764872.745398676, 4238764872.745398675983467.toFixed(9), 0.00000000001)
            Assert.assertEquals(4238764872.745398676, 4238764872.745398675983467.toFixed(9), 0.00000000001)
            Assert.assertEquals(4238764873.0, 4238764872.745398675983467.toFixed(0), 0.000001)
    }

    @Test
    fun toFixed2() {
            Assert.assertEquals(0.0, 0.01.toFixed2(1), 0.0001)
            Assert.assertEquals(0.1, 0.07.toFixed2(1), 0.0001)
            Assert.assertEquals(4238764872.75, 4238764872.745398675983467.toFixed2(2), 0.000001)
            Assert.assertEquals(4238764872.74539868, 4238764872.745398675983467.toFixed2(8), 0.0000000001)
        //Assert.assertEquals(4238764872.745398676, 4238764872.745398675983467.toFixed2(9), 0.0000001)
            Assert.assertEquals(4238764873.0, 4238764872.745398675983467.toFixed2(0), 0.000001)
    }
}