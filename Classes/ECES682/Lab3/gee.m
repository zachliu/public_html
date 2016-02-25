function pix = gee(N,M,a,mu,nu,fs)
% Test function generator
delta = 1/fs;
x = ((1:M) - (1+M)/2)*delta;
y = ((1:N) - (1+N)/2)*delta;
pix = zeros(N,M);
for r = 1:N
    pix(r,:) = cos(2*pi*(mu*x+nu*y(r))).*exp(-pi*(x.^2+y(r)^2)/(a^2));
end