#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.tessari.jamrec)

//#include "rs_graphics.rsh"

rs_allocation samples;

uint32_t bufferSize;
float width;
float height;
int32_t offset;
float trackViewWidth;
uint32_t size;
int valMax = 100;

uchar4 __attribute__((kernel)) root(uint32_t x, uint32_t y) {
    uchar4 out = (uchar4){0,0,0,0};

    int start2 = (offset - trackViewWidth / 2);
    int i = start2 + (x * (trackViewWidth / width));
    int v = 1;
    if(i >= 0 && i < size){
        v = rsGetElementAt_short(samples, i);
        //v = abs(v);

        if (valMax < v)
            valMax = v + 5;
        v = v * (height / (valMax * 2));
    }

    if(y > height/2 - v && y < height/2 + v/*&& (y + height/2) < v && y > (height/2-v)*/){
        out.r = 219;
        out.g = 205;
        out.b = 198;
        out.a = 255;
    }

    return out;
}