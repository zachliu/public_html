% ----------- Lab 1 -----------
% Lab 1
% -----------------------------


%% clear workspace and clear command window
clear;
clc;


%% part 1.1
FC = imread('FluorescentCells.jpg');
figure(1);
imshow(FC);


%% part 1.2
[circbw map_circbw] = imread('circbw.tif');
figure(2);
imshow(circbw, map_circbw);

[board map_board] = imread('board.tif');
figure(3);
imshow(board, map_board);

[trees map_trees] = imread('trees.tif');
figure(4);
imshow(trees, map_trees);


%% part 1.3
imwrite(board,'board.jpg');

[board_jpg map_board_jpg] = imread('board.jpg');
figure(5);
imshow(board_jpg, map_board_jpg);


%% part 2
[peppers map_peppers] = imread('peppers.png');
figure(6);
imshow(peppers);

% peppers_ycbcrmap = rgb2ycbcr(map_peppers);
peppers_YCBCR = rgb2ycbcr(peppers);

peppers_YCBCR(:,:,2) = 128;
peppers_YCBCR(:,:,3) = 128;

peppers_RGB = ycbcr2rgb(peppers_YCBCR);
figure(7);
imshow(peppers_RGB);


%% part 3
imwrite(peppers,'peppers_100.jpg','jpeg','Quality',100);
imwrite(peppers,'peppers_67.jpg','jpeg','Quality',67);
imwrite(peppers,'peppers_33.jpg','jpeg','Quality',33);
imwrite(peppers,'peppers_0.jpg','jpeg','Quality',0);


