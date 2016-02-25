function G = bigg( u, v, a, u0, v0 )

    N = numel(u);
    G = zeros(N,N);
    for i = 1:N
        for j = 1:N
            G(i,j) = 0.5 *(exp(-pi*((u(i)-u0)^2+(v(j)-v0)^2)/(a^2)) + ...
                           exp(-pi*((u(i)+u0)^2+(v(j)+v0)^2)/(a^2)));
        end
    end


end