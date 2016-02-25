%% Quantization trials
%
% rms = @(x) mean(x(:).^2)^0.5;
% close all
% eightbit = [0 255];
% label = 1;

%% The image
len = imread('lena256.tif');
lend = double(len);
imshow(len)
title('Original Image')

%%
len4 = 16*(round(lend/16));
figure(2)
imshow(len4, eightbit)
title('Four bit quantization')
imwrite(uint8(len4),'len4.tif')

%% 
figure(3)
noise = 16*(rand(size(len))-0.5);
len42 = 16*round((lend-noise)/16);
imshow(len42, eightbit)
title('Noise before quantization');
imwrite(uint8(len42),'len42.tif')

%%
figure(7)
len44 = lend+noise;
imshow(len44, eightbit)
title('image plus uniform noise');
imwrite(uint8(len44),'len44.tif')

