%% Star prepare
stara = imread('star1.tif');
starb = imread('star1.jpg');
a = 7;
filt = gee(512,512,a,0,0,1)/(a^2);
cut = 10;
fftfilt = fft2(fftshift(filt));
filr = real(fftfilt);
fili = imag(fftfilt);
filr = max(1/cut, filr);
fili = max(1/cut, fili);
fil = filr + 1i*fili;
fil = 1./fil;
imshow(abs(fftshift(fftfilt)))
starft = fft2(stara);
newft = starft.*fil;
mod = ifft2(newft);
figure
imshow(mod,[0,255])
