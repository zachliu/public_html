% ------------------- Lab 3 -------------------
% Lab 3
% ---------------------------------------------


%% Init
clear;
clc;


%% Part 1.1
x = -10:0.3:10;
y = -10:0.3:10;
u0 = 0;
v0 = 0;

g1 = smallg( x, y, -0.1, u0, v0 );
g2 = smallg( x, y, 0.3, u0, v0 );
g3 = smallg( x, y, 1, u0, v0 );
g4 = smallg( x, y, 3, u0, v0 );
g5 = smallg( x, y, 5, u0, v0 );
g6 = smallg( x, y, 10, u0, v0 );

figure(1);
subplot(2,3,1);
mesh(g1);
title('a = -0.1');
subplot(2,3,2);
mesh(g2);
title('a = 0.3');
subplot(2,3,3);
mesh(g3);
title('a = 1');
subplot(2,3,4);
mesh(g4);
title('a = 3');
subplot(2,3,5);
mesh(g5);
title('a = 5');
subplot(2,3,6);
mesh(g6);
title('a = 10');


%% Part 1.2
u = -10:0.3:10;
v = -10:0.3:10;
u0 = 0;
v0 = 0;

G1 = bigg( u, v, -0.1, u0, v0 );
G2 = bigg( u, v, 0.3, u0, v0 );
G3 = bigg( u, v, 1, u0, v0 );
G4 = bigg( u, v, 3, u0, v0 );
G5 = bigg( u, v, 5, u0, v0 );
G6 = bigg( u, v, 10, u0, v0 );

figure(2);
subplot(2,3,1);
mesh(G1);
title('a = -0.1');
subplot(2,3,2);
mesh(G2);
title('a = 0.3');
subplot(2,3,3);
mesh(G3);
title('a = 1');
subplot(2,3,4);
mesh(G4);
title('a = 3');
subplot(2,3,5);
mesh(G5);
title('a = 5');
subplot(2,3,6);
mesh(G6);
title('a = 10');


%% Part 1.3
x = -10:0.3:10;
y = -10:0.3:10;
u = -10:0.3:10;
v = -10:0.3:10;

g1 = smallg( x, y, 10, 5, 0 );
g2 = smallg( x, y, 10, 3, 4 );
g3 = smallg( x, y, 10, -3, 4 );

G1 = bigg( u, v, 10, 5, 0 );
G2 = bigg( u, v, 10, 3, 4 );
G3 = bigg( u, v, 10, -3, 4 );

figure(3);
subplot(2,3,1);
mesh(g1);
title('u0 = 5, v0 = 0');
subplot(2,3,2);
mesh(g2);
title('u0 = 3, v0 = 4');
subplot(2,3,3);
mesh(g3);
title('u0 = -3, v0 = 4');
subplot(2,3,4);
mesh(G1);
title('u0 = 5, v0 = 0');
subplot(2,3,5);
mesh(G2);
title('u0 = 3, v0 = 4');
subplot(2,3,6);
mesh(G3);
title('u0 = -3, v0 = 4');


%% Part 2
lena = imread('lena1.jpg');
figure(4);
subplot(1,2,1);
imshow(lena);
title('Image before filtering');

lena_F = fft2(fftshift(lena));
lena_F = fftshift(lena_F);

top = max(abs(lena_F(:)));
bot = min(abs(lena_F(:)));
ftpix = (lena_F-bot)/(top-bot);

figure(5);
subplot(1,2,1);
imshow(abs(ftpix),[0 1],'InitialMagnification','fit'); colormap(jet); colorbar
title('Image FFT before filtering');

lena_F(252,252) = -177.385379670970 + 390.374021855902i;
lena_F(262,262) = -177.385379670970 + 390.374021855902i;

lena_f = ifft2(lena_F);
lena_f = uint8(abs(lena_f));

lena_f = fftshift(lena_f);
figure(4);
subplot(1,2,2);
imshow(lena_f);
title('Image after filtering');

top = max(abs(lena_F(:)));
bot = min(abs(lena_F(:)));
ftpix = (lena_F-bot)/(top-bot);
figure(5);
subplot(1,2,2);
imshow(abs(ftpix),[0 1],'InitialMagnification','fit'); colormap(jet); colorbar
title('Image FFT after filtering');


%% Part 3
g1p = fftshift(g1);
G1p = fft2(g1p);
G1p = fftshift(G1p);
top = max(abs(G1p(:)));
bot = min(real(G1p(:)));
G1ppix = (G1p-bot)/(top-bot);
figure(4);
subplot(1,3,1);
imshow(real(G1ppix));

g2p = fftshift(g2);
G2p = fft2(g2p);
G2p = fftshift(G2p);
top = max(abs(G2p(:)));
bot = min(real(G2p(:)));
G2ppix = (G2p-bot)/(top-bot);
figure(4);
subplot(1,3,2);
imshow(real(G2ppix));

g3p = fftshift(g3);
G3p = fft2(g3);
G3p = fftshift(G3p);
top = max(abs(G3p(:)));
bot = min(real(G3p(:)));
G3ppix = (G3p-bot)/(top-bot);
figure(4);
subplot(1,3,3);
imshow(real(G3ppix));

