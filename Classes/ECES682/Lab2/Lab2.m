% --------------------- Lab 2 ---------------------
% Lab 2
% -------------------------------------------------


%% clear workspace and clear command window
clear;
clc;


%% Part 1a
Rice = imread('rice.png');
imfinfo('rice.png')
figure(1);
subplot(1,2,1);
imshow(Rice);
subplot(1,2,2);
imhist(Rice,64);


Adj_Rice = imadjust(Rice);
figure(2);
subplot(1,2,1);
imshow(Adj_Rice);
subplot(1,2,2);
imhist(Adj_Rice,64);


%% Part 1a
Ima_102_5_15 = imread('102-5-15.ima.tif');
imfinfo('102-5-15.ima.tif')
figure(3);
subplot(1,2,1);
imshow(Ima_102_5_15);
subplot(1,2,2);
imhist(Ima_102_5_15,1024);


Adj_Ima_102_5_15 = imadjust(Ima_102_5_15);
figure(4);
subplot(1,2,1);
imshow(Adj_Ima_102_5_15);
subplot(1,2,2);
imhist(Adj_Ima_102_5_15,64);


%% Part 2a circuit.tif
Circ = imread('circuit.tif');
figure(5);
subplot(2,2,1);
imshow(Circ);
title('Original Image');
[ Circ_Edge_1, TH1s ] = edge( Circ, 'sobel', 0.06 );
subplot(2,2,2);
imshow(Circ_Edge_1);
title('Sobel #1');
[ Circ_Edge_2, TH2s ] = edge( Circ, 'sobel', 0.08 );
subplot(2,2,3);
imshow(Circ_Edge_2);
title('Sobel #2');
[ Circ_Edge_3, TH3s ] = edge( Circ, 'sobel', 0.10 );
subplot(2,2,4);
imshow(Circ_Edge_3);
title('Sobel #3');

figure(6);
subplot(2,2,1);
imshow(Circ);
title('Original Image');
[ Circ_Edge_1, TH1c ] = edge( Circ, 'canny', [0.1 0.3] );
subplot(2,2,2);
imshow(Circ_Edge_1);
title('Canny #1');
[ Circ_Edge_2, TH2c ] = edge( Circ, 'canny', [0.1063 0.2656] );
subplot(2,2,3);
imshow(Circ_Edge_2);
title('Canny #2');
[ Circ_Edge_3, TH3c ] = edge( Circ, 'canny', [0.05 0.4] );
subplot(2,2,4);
imshow(Circ_Edge_3);
title('Canny #3');


%% Part 2b Eagle.tif
Eagle2 = imread('Eagle2.tif');
% imfinfo('Eagle2.tif')
figure(7);
subplot(1,2,1);
imshow(Eagle2);
subplot(1,2,2);
Eagle2_gray = rgb2gray(Eagle2);
imshow(Eagle2_gray);


figure(8);
subplot(2,2,1);
imshow(Eagle2_gray);
title('Original Image');
[ Eagle2_gray_edge_1, TH1s ] = edge( Eagle2_gray, 'sobel', 0.06 );
subplot(2,2,2);
imshow(Eagle2_gray_edge_1);
title('Sobel #1');
[ Eagle2_gray_edge_2, TH2s ] = edge( Eagle2_gray, 'sobel', 0.0732 );
subplot(2,2,3);
imshow(Eagle2_gray_edge_2);
title('Sobel #2');
[ Eagle2_gray_edge_3, TH3s ] = edge( Eagle2_gray, 'sobel', 0.10 );
subplot(2,2,4);
imshow(Eagle2_gray_edge_3);
title('Sobel #3');

figure(9);
subplot(2,2,1);
imshow(Eagle2_gray);
title('Original Image');
[ Eagle2_gray_edge_1, TH1c ] = edge( Eagle2_gray, 'canny', [0.0113 0.0981] );
subplot(2,2,2);
imshow(Eagle2_gray_edge_1);
title('Canny #1');
[ Eagle2_gray_edge_2, TH2c ] = edge( Eagle2_gray, 'canny', [0.0313 0.0781] );
subplot(2,2,3);
imshow(Eagle2_gray_edge_2);
title('Canny #2');
[ Eagle2_gray_edge_3, TH3c ] = edge( Eagle2_gray, 'canny', [0.01 0.1] );
subplot(2,2,4);
imshow(Eagle2_gray_edge_3);
title('Canny #3');


%% Part 3
Tsc = imread('Tommy screen.tif');
Tsp = imread('Tommy speckle.tif');

Tsc_f_1 = filter2(fspecial('average',[3 3]),Tsc)/255;
Tsp_f_1 = filter2(fspecial('average',[3 3]),Tsp)/255;

figure(10);
subplot(2,2,1);
imshow(Tsc);
title('Original Tommy Screen');
subplot(2,2,2);
imshow(Tsp);
title('Original Tommy Speckle');
subplot(2,2,3);
imshow(Tsc_f_1);
title({'Filtered Tommy Screen','using filter2'});
subplot(2,2,4);
imshow(Tsp_f_1);
title({'Filtered Tommy Speckle','using filter2'});

Tsc_f_2 = medfilt2(Tsc,[3 3]);
Tsp_f_2 = medfilt2(Tsp,[3 3]);

figure(11);
subplot(2,2,1);
imshow(Tsc);
title('Original Tommy Screen');
subplot(2,2,2);
imshow(Tsp);
title('Original Tommy Speckle');
subplot(2,2,3);
imshow(Tsc_f_2);
title({'Filtered Tommy Screen','using medfilt2'});
subplot(2,2,4);
imshow(Tsp_f_2);
title({'Filtered Tommy Speckle','using medfilt2'});

