clear;
clc;
load out.xpm;

count=0;
for i=1:size(X,1)
    for j=1:size(X,2)
        if ~isnan(Z(i,j))
            count=count+1;
            xx(count,1)=X(i,j);
            yy(count,1)=Y(i,j);
            zz(count,1)=Z(i,j);
            dd(count,1)=X(i,j);
            dd(count,2)=Y(i,j);
            dd(count,3)=Z(i,j);
        else
            count=count+1;
            xx(count,1)=X(i,j);
            yy(count,1)=Y(i,j);
            zz(count,1)=0;
            dd(count,1)=X(i,j);
            dd(count,2)=Y(i,j);
            dd(count,3)=0;         
        end
    end
end

for i = 1:501
	figure(1);
	plot3(i,j,out(i,j)); hold on
end