#include <stdint.h>
#include <stddef.h>

__declspec(dllexport)
void invert_image(uint8_t* image, int length) {
    for (int i = 0; i < length; i++){
         image[i] = 255 - image[i];
    }
}

__declspec(dllexport)
void tint_image(uint8_t* image, int length, uint8_t redScale, uint8_t greenScale, uint8_t blueScale) {
    for (int i = 0; i + 2 < length; i += 3) {
        image[i]   = (image[i]   * redScale)   / 255; // R
        image[i+1] = (image[i+1] * greenScale) / 255; // G
        image[i+2] = (image[i+2] * blueScale)  / 255; // B
    }
}