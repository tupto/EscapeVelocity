import com.sun.org.apache.xpath.internal.operations.Or;

public class OrbitalData {
    private Vector2 periapsis;
    private Vector2 apoapsis;
    private Vector2 relativeVelocity;
    private double eccentricity;
    private double semiMajorAxis;
    private double semiMinorAxis;
    private double escapeVelocity;
    private double distance;

    private OrbitalData() { }

    public static OrbitalData calculateOrbit(Vector2 origin, Vector2 distance, Vector2 velocity, double gravParam) {
        Vector2 rV = new Vector2(distance);
        double rM = rV.getMagnitude();
        Vector2 vV = new Vector2(velocity);
        double vM = vV.getMagnitude();

        double h = new Vector2(rV).dot(vV);

        Vector2 e1 = new Vector2(rV).multiply(vM * vM).divide(gravParam);
        Vector2 e2 = new Vector2(vV).multiply(rV.dot(vV)).divide(gravParam);
        Vector2 e3 = new Vector2(rV).divide(rM);

        Vector2 eV = e1.subtract(e2).subtract(e3);
        double eM = eV.getMagnitude();

        double orbitalEnergy = ((vM * vM) / 2) - (gravParam / rM);
        double a = gravParam / (2 * orbitalEnergy);

        Vector2 p = new Vector2(eV).divide(eM);

        Vector2 periapsis = new Vector2(0).subtract(new Vector2(p).multiply(a * (1 - eM))).add(origin);
        Vector2 apoapsis = new Vector2(0).subtract(new Vector2(p).multiply(-a * (1 + eM))).add(origin);

        double escapeVelocity = Math.sqrt(2*gravParam / rM);

        OrbitalData data = new OrbitalData();
        data.apoapsis = apoapsis;
        data.periapsis = periapsis;
        data.eccentricity = eM;
        data.semiMajorAxis = a;
        data.semiMinorAxis = a;
        data.escapeVelocity = escapeVelocity;
        data.distance = rM;
        data.relativeVelocity = velocity;
        return data;
    }

    public Vector2 getPeriapsis() {
        return new Vector2(periapsis);
    }

    public Vector2 getApoapsis() {
        return new Vector2(apoapsis);
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getSemiMinorAxis() {
        return semiMinorAxis;
    }

    public double getEscapeVelocity() {
        return escapeVelocity;
    }

    public double getDistance() {
        return distance;
    }

    public Vector2 getRelativeVelocity() {
        return relativeVelocity;
    }
}
