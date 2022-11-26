package de.fabmax.physxjni;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;
import physx.common.PxIDENTITYEnum;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.PxScene;
import physx.vehicle2.*;

import java.util.Arrays;

public class VehicleTest {

    @Test
    public void vehicleTest() {
        PxScene scene = PhysXTestEnv.createEmptyScene();
        var groundPlane = PhysXTestEnv.createGroundPlane();
        scene.addActor(groundPlane);

        EngineDriveVehicle vehicle4wd = new EngineDriveVehicle();
        setBaseParams(vehicle4wd.getBaseParams());
        setEngineDriveParams(vehicle4wd.getEngineDriveParams(), vehicle4wd.getBaseParams().getAxleDescription());

        Assertions.assertTrue(
                vehicle4wd.initialize(
                        PhysXTestEnv.physics,
                        PhysXTestEnv.cookingParams,
                        PhysXTestEnv.defaultMaterial,
                        EngineDriveVehicleEnum.eDIFFTYPE_FOURWHEELDRIVE
                )
        );
    }

    private void setBaseParams(BaseVehicleParams baseParams) {

        //
        // most values taken from Physx/physx/snippets/media/vehicledata/Base.json
        //

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var axleDesc = baseParams.getAxleDescription();
            axleDesc.setNbAxles(2);
            axleDesc.setNbWheels(4);
            axleDesc.setNbWheelsPerAxle(0, 2);
            axleDesc.setNbWheelsPerAxle(1, 2);
            for (int i = 0; i < 4; i++) {
                axleDesc.setAxleToWheelIds(i, i);
                axleDesc.setWheelIdsInAxleOrder(i, i);
            }
            Assertions.assertTrue(axleDesc.isValid());

            var frame = baseParams.getFrame();
            frame.setLatAxis(PxVehicleAxesEnum.ePosX);
            frame.setLngAxis(PxVehicleAxesEnum.ePosZ);
            frame.setVrtAxis(PxVehicleAxesEnum.ePosY);
            Assertions.assertTrue(frame.isValid());

            baseParams.getScale().setScale(1f);

            var rigidBody = baseParams.getRigidBodyParams();
            rigidBody.setMass(2000f);
            rigidBody.setMoi(PxVec3.createAt(stack, MemoryStack::nmalloc, 3200f, 3400f, 750f));
            Assertions.assertTrue(rigidBody.isValid());

            var brakeResponse = baseParams.getBrakeResponseParams(0);
            brakeResponse.setMaxResponse(2000f);
            for (int i = 0; i < 4; i++) {
                brakeResponse.setWheelResponseMultipliers(i, 1f);
            }
            Assertions.assertTrue(brakeResponse.isValid(axleDesc));

            var handBrakeResponse = baseParams.getBrakeResponseParams(0);
            handBrakeResponse.setMaxResponse(2000f);
            for (int i = 0; i < 4; i++) {
                handBrakeResponse.setWheelResponseMultipliers(i, i < 2 ? 1f : 0f);
            }
            Assertions.assertTrue(handBrakeResponse.isValid(axleDesc));

            var steerResponse = baseParams.getSteerResponseParams();
            steerResponse.setMaxResponse(0.5f);
            for (int i = 0; i < 4; i++) {
                steerResponse.setWheelResponseMultipliers(i, i < 2 ? 1f : 0f);
            }
            Assertions.assertTrue(steerResponse.isValid(axleDesc));

            var ackermann = baseParams.getAckermannParams(0);
            ackermann.setWheelIds(0, 0);
            ackermann.setWheelIds(1, 1);
            ackermann.setWheelBase(2.8f);
            ackermann.setTrackWidth(1.6f);
            ackermann.setStrength(1f);
            Assertions.assertTrue(ackermann.isValid(axleDesc));

            var suspensionAttachmentPoses = Arrays.asList(
                    PxTransform.createAt(stack, MemoryStack::nmalloc,
                            PxVec3.createAt(stack, MemoryStack::nmalloc, -0.8f, -0.1f, 1.4f),
                            PxQuat.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity)
                    ),
                    PxTransform.createAt(stack, MemoryStack::nmalloc,
                            PxVec3.createAt(stack, MemoryStack::nmalloc, 0.8f, -0.1f, 1.4f),
                            PxQuat.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity)
                    ),
                    PxTransform.createAt(stack, MemoryStack::nmalloc,
                            PxVec3.createAt(stack, MemoryStack::nmalloc, -0.8f, -0.1f, -1.4f),
                            PxQuat.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity)
                    ),
                    PxTransform.createAt(stack, MemoryStack::nmalloc,
                            PxVec3.createAt(stack, MemoryStack::nmalloc, -0.8f, -0.1f, -1.4f),
                            PxQuat.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity)
                    )
            );
            var wheelAttachmentPose = PxTransform.createAt(stack, MemoryStack::nmalloc,
                    PxVec3.createAt(stack, MemoryStack::nmalloc, 0f, 0f, 0f),
                    PxQuat.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity)
            );
            var suspensionTravelDir = PxVec3.createAt(stack, MemoryStack::nmalloc, 0f, -1f, 0f);
            for (int i = 0; i < 4; i++) {
                var suspension = baseParams.getSuspensionParams(i);
                suspension.setSuspensionAttachment(suspensionAttachmentPoses.get(i));
                suspension.setSuspensionTravelDir(suspensionTravelDir);
                suspension.setWheelAttachment(wheelAttachmentPose);
                suspension.setSuspensionTravelDist(0.25f);
                Assertions.assertTrue(suspension.isValid());
            }

            var suspensionCalc = baseParams.getSuspensionStateCalculationParams();
            suspensionCalc.setSuspensionJounceCalculationType(PxVehicleSuspensionJounceCalculationTypeEnum.eSWEEP);
            suspensionCalc.setLimitSuspensionExpansionVelocity(false);
            Assertions.assertTrue(suspensionCalc.isValid());

            var forceAppPoint = PxVec3.createAt(stack, MemoryStack::nmalloc, 0f, 0f, -0.11f);
            for (int i = 0; i < 4; i++) {
                var suspensionComp = baseParams.getSuspensionComplianceParams(i);
                suspensionComp.getWheelToeAngle().addPair(0f, 0f);
                suspensionComp.getWheelCamberAngle().addPair(0f, 0f);
                suspensionComp.getSuspForceAppPoint().addPair(0f, forceAppPoint);
                suspensionComp.getTireForceAppPoint().addPair(0f, forceAppPoint);
                Assertions.assertTrue(suspensionComp.isValid());
            }

            for (int i = 0; i < 4; i++) {
                var suspensionForce = baseParams.getSuspensionForceParams(i);
                suspensionForce.setDamping(8_000f);
                suspensionForce.setStiffness(32_000f);
                suspensionForce.setSprungMass(500f);
                Assertions.assertTrue(suspensionForce.isValid());
            }

            for (int i = 0; i < 4; i++) {
                var tireForce = baseParams.getTireForceParams(i);
                tireForce.setLongStiff(25_000f);
                tireForce.setLatStiffX(0.01f);
                tireForce.setLatStiffY(120_000f);
                tireForce.setCamberStiff(0f);
                tireForce.setRestLoad(500f);
                PxVehicleTireForceParamsExt.setFrictionVsSlip(tireForce, 0, 0, 0f);
                PxVehicleTireForceParamsExt.setFrictionVsSlip(tireForce, 0, 1, 1f);
                PxVehicleTireForceParamsExt.setFrictionVsSlip(tireForce, 1, 0, 0.1f);
                PxVehicleTireForceParamsExt.setFrictionVsSlip(tireForce, 1, 1, 1f);
                PxVehicleTireForceParamsExt.setFrictionVsSlip(tireForce, 2, 0, 1f);
                PxVehicleTireForceParamsExt.setFrictionVsSlip(tireForce, 2, 1, 1f);

                PxVehicleTireForceParamsExt.setLoadFilter(tireForce, 0, 0, 0f);
                PxVehicleTireForceParamsExt.setLoadFilter(tireForce, 0, 1, 0.23f);
                PxVehicleTireForceParamsExt.setLoadFilter(tireForce, 1, 0, 3f);
                PxVehicleTireForceParamsExt.setLoadFilter(tireForce, 1, 1, 3f);
                Assertions.assertTrue(tireForce.isValid());
            }

            for (int i = 0; i < 4; i++) {
                var wheel = baseParams.getWheelParams(i);
                wheel.setMass(25f);
                wheel.setRadius(0.35f);
                wheel.setHalfWidth(0.15f);
                wheel.setDampingRate(0.25f);
                wheel.setMoi(1.17f);
                Assertions.assertTrue(wheel.isValid());
            }

            Assertions.assertTrue(baseParams.isValid());
        }
    }

    private void setEngineDriveParams(EngineDrivetrainParams engineDriveParams, PxVehicleAxleDescription axleDesc) {

        //
        // most values taken from Physx/physx/snippets/media/vehicledata/EngineDrive.json
        //

        var autobox = engineDriveParams.getAutoboxParams();
        autobox.setUpRatios(0, 0.65f);
        autobox.setUpRatios(1, 0.15f);
        autobox.setUpRatios(2, 0.65f);
        autobox.setUpRatios(3, 0.65f);
        autobox.setUpRatios(4, 0.65f);
        autobox.setUpRatios(5, 0.65f);
        autobox.setUpRatios(6, 0.65f);
        autobox.setDownRatios(0, 0.5f);
        autobox.setDownRatios(1, 0.5f);
        autobox.setDownRatios(2, 0.5f);
        autobox.setDownRatios(3, 0.5f);
        autobox.setDownRatios(4, 0.5f);
        autobox.setDownRatios(5, 0.5f);
        autobox.setDownRatios(6, 0.5f);
        autobox.setLatency(2f);

        engineDriveParams.getClutchCommandResponseParams().setMaxResponse(10f);

        var engine = engineDriveParams.getEngineParams();
        engine.getTorqueCurve().addPair(0f, 1f);
        engine.getTorqueCurve().addPair(0.33f, 1f);
        engine.getTorqueCurve().addPair(1f, 1f);
        engine.setMoi(1f);
        engine.setPeakTorque(500f);
        engine.setIdleOmega(0f);
        engine.setMaxOmega(600f);
        engine.setDampingRateFullThrottle(0.15f);
        engine.setDampingRateZeroThrottleClutchEngaged(2f);
        engine.setDampingRateZeroThrottleClutchDisengaged(0.35f);

        var gearbox = engineDriveParams.getGearBoxParams();
        gearbox.setNeutralGear(1);
        gearbox.setRatios(0, -4f);
        gearbox.setRatios(1, 0f);
        gearbox.setRatios(2, 4f);
        gearbox.setRatios(3, 2f);
        gearbox.setRatios(4, 1.5f);
        gearbox.setRatios(5, 1.1f);
        gearbox.setRatios(6, 1f);
        gearbox.setNbRatios(7);
        gearbox.setFinalRatio(4f);
        gearbox.setSwitchTime(0.5f);

        var fourWheelDiff = engineDriveParams.getFourWheelDifferentialParams();
        for (int i = 0; i < 4; i++) {
            fourWheelDiff.setTorqueRatios(i, 025f);
            fourWheelDiff.setAveWheelSpeedRatios(i, 025f);
        }
        fourWheelDiff.setFrontWheelIds(0, 0);
        fourWheelDiff.setFrontWheelIds(1, 1);
        fourWheelDiff.setRearWheelIds(0, 2);
        fourWheelDiff.setRearWheelIds(1, 3);
        fourWheelDiff.setCenterBias(1.3f);
        fourWheelDiff.setCenterTarget(1.29f);
        fourWheelDiff.setFrontBias(1.3f);
        fourWheelDiff.setFrontTarget(1.29f);
        fourWheelDiff.setRearBias(1.3f);
        fourWheelDiff.setRearTarget(1.29f);
        fourWheelDiff.setRate(10f);

        var clutch = engineDriveParams.getClutchParams();
        clutch.setAccuracyMode(PxVehicleClutchAccuracyModeEnum.eESTIMATE);
        clutch.setEstimateIterations(5);

        Assertions.assertTrue(engineDriveParams.isValid(axleDesc));
    }
}