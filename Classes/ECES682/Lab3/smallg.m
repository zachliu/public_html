function g = smallg( x, y, a, u0, v0 )

    N = numel(x);
    g = zeros(N,N);
    for i = 1:N
        for j = 1:N
            g(i,j) = cos(2*pi*(u0*x(i)+v0*y(j))) * exp(-pi*(x(i)^2+y(j)^2)/(a^2));
        end
    end


end