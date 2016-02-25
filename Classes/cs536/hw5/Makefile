OBJS = cs536_hw5.o
CC = g++
DEBUG = -g
CFLAGS = -Wall -c $(DEBUG)
LFLAGS = -Wall $(DEBUG)

p1 : $(OBJS)
	$(CC) $(LFLAGS) $(OBJS) -o cs536_hw5

cs536_hw5.o : cs536_hw5.cpp
	$(CC) $(CFLAGS) cs536_hw5.cpp

clean:
	\rm *.o *~ p1