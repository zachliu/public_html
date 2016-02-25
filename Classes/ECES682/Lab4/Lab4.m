% ------------------------------ Lab 4 ------------------------------------ 
% Lab 4
% -------------------------------------------------------------------------


%% Init
clear;
clc;


%% Part 1
lena256 = imread('lena256.tif');
figure(1);
subplot(1,2,1);
imshow(lena256);
title('"lena256.tif" original file');

N = 51;
cnt = 1;
lena256jpg_fsize = zeros(N,1);
SNRdb = zeros(N,1);
for Qua = 0:2:100
    imwrite(lena256, 'lena256.jpg', 'Quality', Qua);
    lena256jpg_info = imfinfo('lena256.jpg');
    lena256jpg_fsize(cnt) = lena256jpg_info.FileSize / 1024; % file size in kilo
    lena256jpg = imread('lena256.jpg');

    imax = double( max( lena256jpg(:) ) );
    imin = double( min( lena256jpg(:) ) );

    E = 0;
    for i = 1:size(lena256jpg,1)
        for j = 1:size(lena256jpg,2)
            E = E + ( double(lena256jpg(i,j)) - double(lena256(i,j)) )^2;
        end
    end
    rms = sqrt( E/(size(lena256jpg,1)*size(lena256jpg,2)) );

    SNR = (imax-imin) / rms;
    SNRdb(cnt) = 20 * log10(SNR);
    cnt = cnt + 1;
end
figure(1);
subplot(1,2,2);
imshow(lena256jpg);
title('"lena256.jpg" rewrite file');

figure(2);
plot(lena256jpg_fsize, SNRdb, '-ob');
grid on;
title('File Size vs SNR');
xlabel('File Size (Kb)');
ylabel('SNR(dB)');


%% Part 2
eightbit = [0 255];
len = imread('lena256.tif');
lend = double(len);
figure(3);
subplot(2,2,1);
imshow(len)
title('Original Image')
% -------------------------------------------------------------------------
len4 = 16*(round(lend/16));
subplot(2,2,2);
imshow(len4, eightbit)
title('Four bit quantization')
imwrite(uint8(len4),'len4.tif')

E1 = 0;
for i = 1:size(lend,1)
    for j = 1:size(lend,2)
        E1 = E1 + ( len4(i,j) - lend(i,j) )^2;
    end
end
rms_1 = sqrt( E1/(size(lend,1)*size(lend,2)) );
xlabel(['RMS = ', num2str(rms_1)]);
% -------------------------------------------------------------------------
noise = 16*(rand(size(len))-0.5);
len42 = 16*round((lend-noise)/16);
subplot(2,2,3);
imshow(len42, eightbit)
title('Noise before quantization');
imwrite(uint8(len42),'len42.tif')

E2 = 0;
for i = 1:size(lend,1)
    for j = 1:size(lend,2)
        E2 = E2 + ( len42(i,j) - lend(i,j) )^2;
    end
end
rms_2 = sqrt( E2/(size(lend,1)*size(lend,2)) );
xlabel(['RMS = ', num2str(rms_2)]);
% -------------------------------------------------------------------------
len44 = lend+noise;
subplot(2,2,4);
imshow(len44, eightbit)
title('image plus uniform noise');
imwrite(uint8(len44),'len44.tif')

E3 = 0;
for i = 1:size(lend,1)
    for j = 1:size(lend,2)
        E3 = E3 + ( len44(i,j) - lend(i,j) )^2;
    end
end
rms_3 = sqrt( E3/(size(lend,1)*size(lend,2)) );
xlabel(['RMS = ', num2str(rms_3)]);


%% Part 3 I
star = imread('star40_512.tif');
figure(4);
imshow(star);
a = 7;
filt = gee(512,512,a,0,0,1)/(a^2);
fftfilt = fft2(fftshift(filt));
figure(5);
subplot(1,2,1);
imshow(abs(fftshift(fftfilt)))
starft = fft2(star);
newft = starft.*fftfilt;
mod = ifft2(newft);
subplot(1,2,2);
imshow(mod,[0,255])
imwrite(uint8(mod),'star1.tif')
imwrite(uint8(mod),'star1.jpg')


%% Part 3 II
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
figure(6);
subplot(1,2,1);
imshow(abs(fftshift(fftfilt)))
starft = fft2(starb);
newft = starft.*fil;
mod = ifft2(newft);
subplot(1,2,2);
imshow(abs(mod),[0,255])

