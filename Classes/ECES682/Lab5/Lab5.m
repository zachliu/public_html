% ---------------------------- Lab 5 ----------------------------
% Lab 5
% ---------------------------------------------------------------


%% Init
clear;
clc;


%% Part 1.a
waterfall  = imread('Waterfall.tif');
dct_water  = do_blockdct(waterfall, 8, 1);
idct_water = do_blockdct(dct_water, 8, -1);
MaxAbsDiff = max(max(abs(idct_water-double(waterfall))));
disp(['Maximum of the absolute value of the difference: ', num2str(MaxAbsDiff)]);


%% Part 1.b
DC = 0;
for i = 1:8:640
	for j = 1:8:416 
        DC = DC + abs(dct_water(i,j));
	end
end
aveDC = DC*64/(640*416);
aveNonDC = (sum(sum(dct_water))-DC)/(640*416-640*416/64);

diff = zeros(1,3);


%% Part 1.c
waterfall = imread('Waterfall.tif');
encodewater = do_dctencoding(waterfall, 8, 1/2);
figure(1);
imshow( uint8(encodewater) );
title( '1/4 of the DCT coefficients' );
diff(1,1) = mean(mean(abs(uint8(encodewater)-waterfall)));


%% Part 1.d
encodewater = do_dctencoding(waterfall, 8, 1/3);
figure(2);
imshow(uint8(encodewater));
title('1/9 of the DCT coefficients');
diff(1,2) = mean(mean(abs(uint8(encodewater)-waterfall)));

encodewater = do_dctencoding(waterfall, 8, 1/4);
figure(3);
imshow(uint8(encodewater));
title('1/16 of the DCT coefficients');
diff(1,3) = mean(mean(abs(uint8(encodewater)-waterfall)));

figure(4);
plot([1/4 1/9 1/16],diff,'--mo');
title('the rate-distortion curve');


%% Part 2.a
ima = imread('a.jpg');
imb = imread('b.jpg');
YCBCR = rgb2ycbcr(ima);
Yima = YCBCR(:,:,1);
YCBCR = rgb2ycbcr(imb);
Yimb = YCBCR(:,:,1);
DiffY = abs(Yima-Yimb);
MeanDiffY = mean(mean(DiffY));
MeanYimb = mean(mean(Yimb));
Compression1 = MeanDiffY/MeanYimb;

imwrite(imb,'b.jpeg','Quality',75);
iminf1 = imfinfo('b.jpeg');
iminf2 = imfinfo('b.jpg');
Compression2 = iminf2.FileSize/iminf1.FileSize;


%% Part 2.b
ima = imread('a.jpg');
imb = imread('b.jpg');
figure;imshow(ima)
YCBCRa = rgb2ycbcr(ima);
YCBCRb = rgb2ycbcr(imb);
YCBCRb(:,:,1) = YCBCRb(:,:,1)-YCBCRa(:,:,1);
rms = sqrt(mean(mean(abs(YCBCRa(:,:,1)).^2)));
bfroma = ycbcr2rgb(YCBCRb);
figure(5);
imshow(bfroma)
imwrite(bfroma,'bfroma.jpg');







