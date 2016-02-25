function out = do_dctencoding(in, b_size, coeff)
% The input image is not padded: If b_size does not divide dimensions, part
% is left out.
% warning off all;
[h v] = size(in);
out = zeros(h,v);
kk = 1;
in = double(in);
for k = round(b_size:b_size:h)
    mm = 1;
    for m = round(b_size:b_size:v)
        get = in(kk:k,mm:m);
        out1(kk:k,mm:m) = dct2(get);
        out1(kk+b_size*coeff:k,:) = 0;
        out1(:,mm+b_size*coeff:m) = 0;
        mm = mm+b_size;
    end
    kk = kk+b_size;
end

kk = 1;
for k = round(b_size:b_size:h)
    mm = 1;
    for m = round(b_size:b_size:v)
        get = out1(kk:k,mm:m);
        out(kk:k,mm:m) = idct2(get);
        mm = mm+b_size;
    end
    kk = kk+b_size;
end