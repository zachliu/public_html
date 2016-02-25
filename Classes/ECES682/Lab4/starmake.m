%% Star prepare
star = imread('star40_512.tif');
a = 7;
filt = gee(512,512,a,0,0,1)/(a^2);
fftfilt = fft2(fftshift(filt));
imshow(abs(fftshift(fftfilt)))
starft = fft2(star);
newft = starft.*fftfilt;
mod = ifft2(newft);
figure
imshow(mod,[0,255])
imwrite(uint8(mod),'star1.tif')
imwrite(uint8(mod),'star1.jpg')