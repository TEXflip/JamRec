#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.tessari.jamrec)

//#include "rs_graphics.rsh"

rs_allocation samples;

uint32_t bufferSize;
uint32_t width;
uint32_t height;

uchar4 __attribute__((kernel)) root(uint32_t x) {
    uchar4 out = (uchar4){0,0,0,0};

    if(x > 100){
        out.r = 219;
        out.g = 205;
        out.b = 198;
        out.a = 255;
    }

    out.r = rsGetElementAt_short(samples, x);


    return out;
}
