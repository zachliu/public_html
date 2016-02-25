function out = do_blockdct(in, b_size, sw)
% The input image is not padded: If b_size does not divide dimensions, part
% is left out.
[h v] = size(in);
out = zeros(h,v);
kk = 1;
in = double(in);
for k = b_size:b_size:h
    mm = 1;
    for m = b_size:b_size:v
        get = in(kk:k,mm:m);
        if sw>0
            out(kk:k,mm:m) = dct2(get);
        else
            out(kk:k,mm:m) = idct2(get);
        end
        mm = mm+b_size;
    end
    kk = kk+b_size;
end
        