package af;
//import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * FinalBot - a robot by Andy Fleischer
 */
public class FinalBot extends TeamRobot
{
	double energyOfEnemy = 100;
    int moveDirection = 1;
    int radarDir = -1;
    boolean inWall = false;
	boolean nearWall = false;

	//run: TestBot's default behavior
	public void run() {
		setColors(Color.magenta, Color.magenta, Color.magenta);
		setAdjustGunForRobotTurn(true); //Sets gun to turn independent from the robot's turn
		setAdjustRadarForGunTurn(true); //Sets radar to turn independent from the gun's turn
	    while (true) {
			turnRadarRight(360); //Scan the area
		}
	}
	
	//onScannedRobot: What to do when you see another robot
	public void onScannedRobot(ScannedRobotEvent event) {
	
		if (isTeammate(event.getName())) {
			return;
		}
	
		//if near a wall, reverse direction
		if (getX() < 50 || getY() < 50 || getX() > getBattleFieldWidth() - 50 || getY() > getBattleFieldHeight() - 50) {
	    //make sure it hasn't been called already
			if (!nearWall) {
	        	reverseDirection();
	       		nearWall = true;
	   		}
		}
		else {
	   		nearWall = false;
		}
	
		//slightly curved line movement
	    if (event.getDistance() < 100){
	        setTurnRight(event.getBearing() + 90  + (15 * moveDirection));
		}
	    else {
	        setTurnRight(event.getBearing() + (90 * moveDirection));
		}
	
		//every 32 ticks or if target shot a bullet, change direction
		double changeInEngergy = energyOfEnemy - event.getEnergy();
	    if (getTime() % 32 == 0 || changeInEngergy > 0 && changeInEngergy <= 3) {
	        moveDirection *= -1;
	    	setAhead(1000 * moveDirection);
	    }
	
		//heading = degrees from north robot body is facing
		//event.bearing = degrees robot needs to turn to face enemy
	
	    setTurnRadarRight(Utils.normalRelativeAngleDegrees(getHeading() + event.getBearing() - getRadarHeading())); //track enemy with radar
	    double enemyAbsoluteBearing = getHeadingRadians() + event.getBearingRadians(); //enemy's bearing relative to North
	    double enemyLateralVelocity = event.getVelocity() * Math.sin(event.getHeadingRadians() - enemyAbsoluteBearing); //enemy's velocity perpendicular to my bot
		setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getGunHeadingRadians() + (enemyLateralVelocity / 13.0))); //targetting algorithm
	 
		if (getEnergy() < 10){//if low on health, shoot less powerful shots (to take less recoil damage)
	        setFire(0.1);
	    }
	    else {//if enemy within 150 units, shoot hard (3); if enemy past 225 units, shoot weak (2); if in between, shoot depending on distance
			setFire(Math.max(Math.min(450 / event.getDistance(), 3), 2));
	    }
	    //update enemy health, change radar direction, and execute set actions
		energyOfEnemy = event.getEnergy();
	    radarDir *= -1;
	    execute();
	}
	
	//onHitWall: what to do if the robot hits a wall (reverse the direction)
	public void onHitWall(HitWallEvent e) {
	    reverseDirection();
	}
	
	//onHitRobot: what to do if the robot hits another robot (reverse the direction)
	public void onHitRobot(HitWallEvent e) {
		reverseDirection();
	}
	
	//reverseDirection: reverses the direction of the robot (changes moveDirection from positive to negative or vice versa)
	public void reverseDirection() {
	    moveDirection *= -1;
	        setBack(200*moveDirection);
	    }
	}