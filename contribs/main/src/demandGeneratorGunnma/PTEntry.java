package demandGeneratorGunnma;

//basic entry to hold person trip data (Japan) for basic demand generation
//author: parady_UT; adapted from Zurich travel census case

public class PTEntry {
	/*
	 * Data Structure:
	 *
	 * field		data example
	 * ID_PERSON	2601
	 * TRIP_NR		1
	 * S_Time		1
	 * H_X			683520
	 * H_Y			248260
	 * S_X			683520
	 * S_Y			248260
	 * D_X			682080
	 * D_Y			246020
	 * BIKE			2
	 * AGE			51
	 * GENDER		0
	 * LICENSE		1
	 * CAR_AV		1
	 * INC_1000		4.19421907421241
	 * DAY			4
	 * TRIP_MODE	4
	 * TRIP_PURPOSE	1
	 * TRIP_DISTANCE	2.7
	 * TRIP_DURATION	32
	 * ID_TOUR		26011
	 */

	int id_person;
	int id_person2;
	int id_person3;
	int id_person4;
	int tripnum;
	int starttime;
	double h_x;
	double h_y;
	double s_x;
	double s_y;
	double d_x;
	double d_y;
	int bike;
	int age;
	int gender;
	int license;
	int caravailability;
	int car;
	int day;
	int tripmode;
	int trippurpose;
	double tripdistance;
	int tripduration;
	int id_tour;
	int strict;
	int arrivetime;

}