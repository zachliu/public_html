hw4 -f bound-cow.smf > cow_default.xpm
hw4 -f bound-cow.smf -P > cow_parallel.xpm
hw4 -f bound-cow.smf -x 1.5 > cow_x1.5.xpm
hw4 -f bound-cow.smf -x 4.75 -y -3.25 -z 3.3 -P > cow_xyzPar.xpm
hw4 -f bound-cow.smf -X 0.25 -Y -0.15 -Z 0.3 > cow_XYZ.xpm
hw4 -f bound-cow.smf -X 0.25 -Y -0.15 -Z 0.3 -j 103 -k 143 -o 421 -p 379 > cow_XYZjkop.xpm
hw4 -f bound-cow.smf -q -1 -r 1.5 -w -2.0 > cow_q_r_w.xpm
hw4 -f bound-cow.smf -Q 1.5 -R 1 -W .4 > cow_QRW.xpm
hw4 -f bound-cow.smf -u -1.5 -v -0.9 -U 1.2 -V 0.7 > cow_uvUV.xpm