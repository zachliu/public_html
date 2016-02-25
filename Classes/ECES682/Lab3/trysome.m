%% Make images, trnasform, rotate
NR = 10;
for k = 1:NR+1
    theta = pi*(k-1)/NR;
    rho = 0.1; a = 100;
    pix =  gee(256,256,a,rho*cos(theta),rho*sin(theta),1);
    figure(1)
    imshow(((pix+1)/0.5).^3);
    figure(2);
    pix1 = fftshift(pix);
    ft = fft2(pix1);
    ft1 = fftshift(ft);
    top = max(abs(ft1(:)));
    bot = min(real(ft1(:)));
    ftpix = (ft1-bot)/(top-bot);
    imshow(ftpix);
    pause(1)
end
