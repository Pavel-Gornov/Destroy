package com.petrolpark.destroy.client.ponder;

import java.util.List;

import com.petrolpark.destroy.block.AgingBarrelBlock;
import com.petrolpark.destroy.block.CoaxialGearBlock;
import com.petrolpark.destroy.block.DestroyBlocks;
import com.petrolpark.destroy.block.DoubleCardanShaftBlock;
import com.petrolpark.destroy.block.LongShaftBlock;
import com.petrolpark.destroy.block.entity.AgingBarrelBlockEntity;
import com.petrolpark.destroy.block.entity.BubbleCapBlockEntity;
import com.petrolpark.destroy.block.entity.CentrifugeBlockEntity;
import com.petrolpark.destroy.block.entity.DynamoBlockEntity;
import com.petrolpark.destroy.block.entity.PumpjackBlockEntity;
import com.petrolpark.destroy.block.entity.behaviour.ChargingBehaviour;
import com.petrolpark.destroy.client.particle.DestroyParticleTypes;
import com.petrolpark.destroy.client.particle.data.GasParticleData;
import com.petrolpark.destroy.fluid.DestroyFluids;
import com.petrolpark.destroy.item.DestroyItems;
import com.petrolpark.destroy.world.village.DestroyVillagers;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class DestroyScenes {

    private static final FluidStack purpleFluid, blueFluid, redFluid;

    // Define coloured Fluids
    static {
        purpleFluid = new FluidStack(PotionFluid.withEffects(1000, new Potion(), List.of(new MobEffectInstance(MobEffects.REGENERATION))), 1000);
        blueFluid = new FluidStack(PotionFluid.withEffects(500, new Potion(), List.of(new MobEffectInstance(MobEffects.NIGHT_VISION))), 1000);
        redFluid = new FluidStack(PotionFluid.withEffects(500, new Potion(), List.of(new MobEffectInstance(MobEffects.DAMAGE_BOOST))), 1000);
    };

    public static void agingBarrel(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("aging_barrel", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();

        BlockPos barrel = new BlockPos(1, 1, 1);

        scene.world.showSection(util.select.layer(0), Direction.UP);
        scene.idle(10);
        scene.world.showSection(util.select.position(barrel), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(80)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(barrel, Direction.UP));
        scene.idle(100);

        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(barrel, Direction.UP), Pointing.DOWN)
            .rightClick()
            .withItem(new ItemStack(Items.WATER_BUCKET)),
            30
        );
        scene.world.modifyBlockEntity(barrel, AgingBarrelBlockEntity.class, be -> {
            be.getTank().fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
        });
        scene.idle(50);

        ItemStack yeast = DestroyItems.YEAST.asStack();
        scene.world.createItemEntity(util.vector.centerOf(barrel.above(2)), Vec3.ZERO, yeast);
        scene.idle(10);
        scene.world.modifyBlockEntity(barrel, AgingBarrelBlockEntity.class, be -> {
            be.inventory.insertItem(0, yeast, false);
        });
        scene.world.createItemEntity(util.vector.centerOf(barrel.above(2)), Vec3.ZERO, new ItemStack(Items.WHEAT));
        scene.idle(10);

        scene.world.setBlock(barrel, DestroyBlocks.AGING_BARREL.getDefaultState().setValue(AgingBarrelBlock.IS_OPEN, false), false);
        scene.world.modifyBlockEntity(barrel, AgingBarrelBlockEntity.class, be -> {
            be.inventory.clearContent();
            be.getTank().drain(1000, FluidAction.EXECUTE);
            be.getTank().fill(new FluidStack(DestroyFluids.UNDISTILLED_MOONSHINE.get(), 1000), FluidAction.EXECUTE);
        });
        scene.idle(20);

        scene.overlay.showText(80)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(barrel, Direction.UP));

        for (int i = 1; i <= 4; i++) {
            BlockState state = DestroyBlocks.AGING_BARREL.getDefaultState()
                .setValue(AgingBarrelBlock.IS_OPEN, false)
                .setValue(AgingBarrelBlock.PROGRESS, i);
            scene.world.setBlock(barrel, state, false);
            scene.idle(20);
        };
        scene.idle(20);

        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(barrel, Direction.UP), Pointing.DOWN)
            .rightClick(),
            30
        );
        scene.idle(50);
        scene.world.setBlock(barrel, DestroyBlocks.AGING_BARREL.getDefaultState().setValue(AgingBarrelBlock.IS_OPEN, true), false);
        scene.idle(50);

        scene.world.createEntity(w -> {
			Villager villagerEntity = EntityType.VILLAGER.create(w);
			Vec3 v = util.vector.topOf(new BlockPos(1, 0, 0));
            villagerEntity.setVillagerData(new VillagerData(VillagerType.PLAINS, DestroyVillagers.INNKEEPER.get(), 0));
			villagerEntity.setPos(v.x, v.y, v.z);
            villagerEntity.xo = v.x;
            villagerEntity.yo = v.y;
            villagerEntity.zo = v.z;
			return villagerEntity;
		});
        scene.idle(10);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.topOf(new BlockPos(1, 1, 0)))
            .attachKeyFrame();
        scene.idle(120); 

        scene.markAsFinished();
    };

    public static void centrifuge(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("centrifuge", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        // Define Selections
        Selection pipework = util.select.fromTo(2, 1, 1, 2, 5, 3);
        Selection kinetics = util.select.fromTo(1, 1, 2, 1, 3, 5)
            .add(util.select.fromTo(2, 1, 4, 2, 5, 4))
            .add(util.select.position(2, 0, 5));
        BlockPos centrifuge = new BlockPos(2, 3, 3);
        BlockPos denseOutputPump = new BlockPos(2, 3, 2);
        BlockPos lightOutputPump = new BlockPos(2, 2, 3);

        // Pre-fill the input Tank
        scene.world.modifyBlockEntity(new BlockPos(2, 5, 3), FluidTankBlockEntity.class, be -> {
            be.getTankInventory().fill(purpleFluid, FluidAction.EXECUTE);
        });
        // Ensure the Centrifuge faces the right way
        scene.world.modifyBlockEntity(centrifuge, CentrifugeBlockEntity.class, be -> {
            be.setPondering();
            be.attemptRotation(false);
        });

        scene.world.showSection(util.select.fromTo(0, 0, 0, 4, 0, 4), Direction.UP);
        scene.world.showSection(pipework, Direction.DOWN);
        scene.idle(10);
        scene.rotateCameraY(90);
        scene.idle(10);
        scene.world.showSection(kinetics, Direction.NORTH);
        scene.idle(10);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(centrifuge, Direction.EAST))
            .attachKeyFrame();
        scene.world.propagatePipeChange(new BlockPos(2, 4, 3));
        scene.idle(120);
        scene.world.modifyBlockEntity(centrifuge, CentrifugeBlockEntity.class, be -> {
            be.getInputTank().drain(4000, FluidAction.EXECUTE);
            be.sendData();
        });
        scene.world.modifyBlockEntity(centrifuge, CentrifugeBlockEntity.class, be -> be.getDenseOutputTank().fill(blueFluid, FluidAction.EXECUTE));
        scene.world.propagatePipeChange(denseOutputPump);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(denseOutputPump, Direction.EAST))
            .attachKeyFrame();
        scene.idle(120);
        scene.world.modifyBlockEntity(centrifuge, CentrifugeBlockEntity.class, be -> be.getLightOutputTank().fill(redFluid, FluidAction.EXECUTE));
        scene.world.propagatePipeChange(lightOutputPump);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(lightOutputPump, Direction.EAST));
        scene.idle(120);
        scene.markAsFinished();

        // TODO lubrication
    };

    public static void bubbleCap(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("bubble_cap", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        //Define Selections
        Selection distillationTower = util.select.fromTo(2, 1, 1, 2, 3, 1);
        Selection kinetics = util.select.fromTo(2, 1, 2, 3, 3, 5).add(util.select.position(2, 0, 5));
        BlockPos blazeBurner = new BlockPos(1, 1, 1);
        BlockPos bottomBubbleCap = new BlockPos(2, 1, 1);
        BlockPos middleBubbleCap = new BlockPos(2, 2, 1);

        GasParticleData particleData = new GasParticleData(DestroyParticleTypes.DISTILLATION.get(), purpleFluid, 1.7f);

        // Pre-fill the input Tank
        scene.world.modifyBlockEntity(new BlockPos(2, 1, 3), FluidTankBlockEntity.class, be -> {
            be.getTankInventory().fill(purpleFluid, FluidAction.EXECUTE);
        });

        scene.world.showSection(util.select.fromTo(0, 0, 0, 4, 0, 4), Direction.UP);
        ElementLink<WorldSectionElement> distillationTowerElement = scene.world.showIndependentSection(distillationTower, Direction.DOWN);
        scene.idle(10);
        scene.overlay.showText(60)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(middleBubbleCap, Direction.WEST));
        scene.idle(80);
        scene.world.showSection(kinetics, Direction.NORTH);
        scene.world.propagatePipeChange(new BlockPos(2, 1, 2));
        scene.idle(100);
        scene.world.modifyBlockEntity(bottomBubbleCap, BubbleCapBlockEntity.class, be -> {
            be.getTank().drain(2000, FluidAction.EXECUTE);
        });
        scene.effects.emitParticles(VecHelper.getCenterOf(bottomBubbleCap), Emitter.simple(particleData, new Vec3(0f, 0f, 0f)), 1.0f, 10);
        scene.world.modifyBlockEntity(new BlockPos(2, 2, 1), BubbleCapBlockEntity.class, be -> {
            be.getInternalTank().fill(blueFluid, FluidAction.EXECUTE);
        });
        scene.world.modifyBlockEntity(new BlockPos(2, 3, 1), BubbleCapBlockEntity.class, be -> {
            be.getInternalTank().fill(redFluid, FluidAction.EXECUTE);
            be.setTicksToFill(BubbleCapBlockEntity.getTankCapacity() / BubbleCapBlockEntity.getTransferRate());
        });
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(middleBubbleCap, Direction.WEST));
        scene.idle(120);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(new BlockPos(2, 3, 1), Direction.WEST))
            .attachKeyFrame();
        scene.idle(120);
        scene.world.hideSection(kinetics, Direction.SOUTH);
        scene.world.moveSection(distillationTowerElement, new Vec3(0, 1, 0), 20);
        scene.idle(10);
        scene.world.moveSection(scene.world.showIndependentSection(util.select.position(blazeBurner), Direction.EAST), new Vec3(1, 0, 0), 20);
        scene.idle(30);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(bottomBubbleCap, Direction.WEST));
        scene.idle(120);
        scene.markAsFinished();
    };

    public static void coaxialGearShaftless(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("coaxial_gear_shaftless", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.idle(5);
        scene.world.showSection(util.select.position(2, 0, 5), Direction.NORTH);
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(3, 1, 2, 3, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(util.select.position(2, 1, 2), Direction.EAST);
        scene.idle(5);
        scene.overlay.showText(60)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(new BlockPos(2, 1, 2), Direction.WEST));
        scene.idle(80);

        scene.world.hideSection(util.select.position(3, 1, 4), Direction.EAST);
        scene.idle(15);
        ElementLink<WorldSectionElement> belt = scene.world.showIndependentSection(util.select.fromTo(3, 3, 4, 4, 3, 4), Direction.DOWN);
        scene.world.moveSection(belt, new Vec3(0d, -2d, 0d), 10);
        scene.idle(10);
        scene.world.showSection(util.select.fromTo(4, 1, 1, 4, 1, 4), Direction.SOUTH);
        scene.idle(5);

        int[][] cogs = new int[][]{new int[]{3, 1}, new int[]{2, 1}, new int[]{1, 1}, new int[]{1, 2}};
        for (int[] cog : cogs) {
            scene.idle(5);
            scene.world.showSection(util.select.position(cog[0], 1, cog[1]), Direction.EAST);
        };

        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(new BlockPos(1, 1, 2), Direction.UP));
        scene.idle(20);

        scene.effects.rotationDirectionIndicator(util.grid.at(1, 1, 1));
		scene.effects.rotationDirectionIndicator(util.grid.at(1, 1, 2));
        scene.idle(100);

        scene.markAsFinished();
    };

    public static void coaxialGearThrough(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("coaxial_gear_through", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection verticalShaft1 = util.select.fromTo(3, 2, 2, 3, 3, 2);
        scene.world.setKineticSpeed(verticalShaft1, 0);
        scene.world.showSection(verticalShaft1, Direction.DOWN);
        scene.idle(30);

        BlockPos coaxialGear1 = new BlockPos(3, 2, 2);
        BlockPos longShaft1 = new BlockPos(3, 3, 2);

        scene.overlay.showText(60)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(coaxialGear1, Direction.UP));
        scene.idle(20);
        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(coaxialGear1, Direction.NORTH), Pointing.RIGHT)
            .withItem(DestroyBlocks.COAXIAL_GEAR.asStack())
        , 60);
        scene.idle(5);
        scene.world.setBlock(coaxialGear1, DestroyBlocks.COAXIAL_GEAR.getDefaultState().setValue(CoaxialGearBlock.HAS_SHAFT, true), false);
        scene.world.setBlock(longShaft1, DestroyBlocks.LONG_SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Axis.Y).setValue(LongShaftBlock.POSITIVE_AXIS_DIRECTION, false), false);
        scene.idle(55);

        scene.world.showSection(util.select.fromTo(1, 2, 2, 1, 3, 2), Direction.DOWN);
        scene.idle(20);

        BlockPos coaxialGear2 = new BlockPos(1, 2, 2);
        BlockPos longShaft2 = new BlockPos(1, 3, 2);

        scene.overlay.showText(60)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(coaxialGear2, Direction.UP));
        scene.idle(20);
        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(coaxialGear2, Direction.NORTH), Pointing.RIGHT)
            .withItem(AllBlocks.SHAFT.asStack())
        , 60);
        scene.idle(5);
        scene.world.setBlock(coaxialGear2, DestroyBlocks.COAXIAL_GEAR.getDefaultState().setValue(CoaxialGearBlock.HAS_SHAFT, true), false);
        scene.world.setBlock(longShaft2, DestroyBlocks.LONG_SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Axis.Y).setValue(LongShaftBlock.POSITIVE_AXIS_DIRECTION, false), false);
        scene.idle(65);

        scene.overlay.showText(80)
            .text("This text is defined in a language file.")
            .placeNearTarget()
            .attachKeyFrame()
            .colored(PonderPalette.RED);
        scene.idle(100);

        scene.world.showSection(util.select.fromTo(1, 0, 5, 3, 1, 5), Direction.NORTH);
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(1, 1, 4, 3, 1, 4), Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(1, 1, 3, 3, 1, 3), Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(1, 1, 2, 3, 1, 2), Direction.SOUTH);
        scene.world.setKineticSpeed(util.select.position(longShaft1), -32);
        scene.world.setKineticSpeed(util.select.position(longShaft2), -32);
        scene.idle(10);

        scene.overlay.showText(80)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(coaxialGear2, Direction.UP));
        scene.idle(100);

        BlockPos cogwheel = new BlockPos(2, 2, 2);

        scene.world.showSection(util.select.position(5, 0, 2), Direction.WEST);
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(4, 1, 2, 4, 2, 2), Direction.DOWN);
        scene.world.setKineticSpeed(util.select.position(coaxialGear1), 8);
        scene.idle(5);
        scene.world.setKineticSpeed(util.select.position(cogwheel), -8);
        scene.world.showSection(util.select.position(cogwheel), Direction.DOWN);
        scene.world.setKineticSpeed(util.select.position(coaxialGear2), 8);
        scene.idle(25);

        scene.overlay.showText(80)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(coaxialGear2, Direction.EAST));
        scene.idle(20);

        scene.effects.rotationDirectionIndicator(coaxialGear1);
		scene.effects.rotationDirectionIndicator(coaxialGear2);
        scene.effects.rotationDirectionIndicator(longShaft1);
		scene.effects.rotationDirectionIndicator(longShaft2);

        scene.idle(80);

        scene.markAsFinished();
    };

    public static void cooler(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("cooler", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.markAsFinished();
    };

    public static void doubleCardanShaft(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("double_cardan_shaft", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        BlockPos dcs = new BlockPos(2, 1, 2);

        scene.world.showSection(util.select.position(1, 0, 5), Direction.NORTH);
        int[][] shafts = new int[][]{new int[]{2, 5}, new int[]{2, 4}, new int[]{2, 3}, new int[]{2, 2}, new int[]{3, 2}, new int[]{4, 2}};
        for (int[] shaft : shafts) {
            scene.idle(5);
            scene.world.showSection(util.select.position(shaft[0], 1, shaft[1]), Direction.DOWN);
        };

        scene.idle(10);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.centerOf(dcs))
            .attachKeyFrame();
        scene.idle(120);

        Selection secondShaft = util.select.fromTo(0, 1, 2, 1, 1, 2);
        scene.world.showSection(secondShaft, Direction.DOWN);
        scene.idle(20);

        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame();
        scene.idle(20);
        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(dcs, Direction.NORTH), Pointing.RIGHT)
            .withItem(AllItems.WRENCH.asStack())
        , 20);
        scene.idle(5);
        scene.world.setBlock(dcs, DoubleCardanShaftBlock.getBlockstateConnectingDirections(Direction.SOUTH, Direction.UP), false);
        scene.world.setKineticSpeed(util.select.fromTo(3, 1, 2, 4, 1, 2), 0);
        scene.idle(25);
        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(dcs, Direction.NORTH), Pointing.RIGHT)
            .withItem(AllItems.WRENCH.asStack())
        , 20);
        scene.idle(5);
        scene.world.setBlock(dcs, DoubleCardanShaftBlock.getBlockstateConnectingDirections(Direction.SOUTH, Direction.WEST), false);
        scene.world.setKineticSpeed(secondShaft, 16);
        scene.idle(15);

        scene.markAsFinished();
    };  

    public static void dynamoRedstone(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("dynamo_redstone", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        BlockPos dynamo = new BlockPos(2, 2, 3);
        BlockPos redStoneDust = new BlockPos(2, 1, 3);

        scene.world.showSection(util.select.fromTo(0, 1, 0, 4, 2, 5).add(util.select.position(2, 0, 5)), Direction.UP);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(dynamo, Direction.WEST));
        scene.idle(120);
        scene.world.setKineticSpeed(util.select.everywhere(), 256);
        scene.world.setKineticSpeed(util.select.position(2, 4, 2), -256); // Set the one cog which should be going the other way to the correct speed
        scene.effects.indicateRedstone(redStoneDust);
        scene.world.modifyBlock(redStoneDust, state -> state.setValue(BlockStateProperties.POWER, 7), false);
        scene.world.modifyBlockEntityNBT(util.select.position(2, 1, 1), NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 7));
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(dynamo, Direction.WEST))
            .attachKeyFrame();
        scene.idle(120);
        scene.markAsFinished();
    };

    public static void dynamoCharging(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("dynamo_charging", "This text is defined in a language file.");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

        BlockPos dynamo = new BlockPos(2, 3, 2);
        BlockPos depot = new BlockPos(2, 1, 1);
        Selection kinetics = util.select.fromTo(2, 3, 3, 2, 3, 5).add(util.select.fromTo(2, 0, 5, 2, 2, 5));

		ElementLink<WorldSectionElement> depotElement = scene.world.showIndependentSection(util.select.position(depot), Direction.DOWN);
		scene.world.moveSection(depotElement, util.vector.of(0, 0, 1), 0);
		scene.idle(10);

        scene.world.showSection(util.select.position(dynamo), Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(kinetics, Direction.NORTH);
        scene.idle(10);

        Vec3 dynamoSide = util.vector.blockSurface(dynamo, Direction.WEST);
		scene.overlay.showText(60)
			.pointAt(dynamoSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("This text is defined in a language file.");
		scene.idle(70);
		scene.overlay.showText(60)
			.pointAt(dynamoSide.subtract(0, 2, 0))
			.placeNearTarget()
			.text("This text is defined in a language file.");
		scene.idle(50);
		ItemStack cell = DestroyItems.DISCHARGED_VOLTAIC_PILE.asStack();
		scene.world.createItemOnBeltLike(depot, Direction.NORTH, cell);
		Vec3 depotCenter = util.vector.centerOf(depot.south());
		scene.overlay.showControls(new InputWindowElement(depotCenter, Pointing.UP).withItem(cell), 30);
		scene.idle(10);

        scene.world.modifyBlockEntity(dynamo, DynamoBlockEntity.class, be -> 
            be.chargingBehaviour.start(ChargingBehaviour.Mode.BELT, util.vector.blockSurface(depot, Direction.UP))
        );
        scene.idle(60);
        //TODO make dynamo actually render in ponder

        scene.world.modifyBlockEntity(dynamo, DynamoBlockEntity.class, be -> 
            be.chargingBehaviour.running = false
        );
        ItemStack chargedCell = DestroyItems.VOLTAIC_PILE.asStack();
        scene.world.removeItemsFromBelt(depot);
		scene.world.createItemOnBeltLike(depot, Direction.UP, chargedCell);
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(depotCenter, Pointing.UP).withItem(chargedCell), 50);
		scene.idle(60);

        scene.markAsFinished();
    };

    public static void dynamoElectrolysis(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("dynamo_electrolysis", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.markAsFinished();
    };

    public static void planetaryGearset(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("planetary_gearset", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();

        scene.world.showSection(util.select.position(1, 0, 3), Direction.NORTH);
        scene.idle(5);
        scene.world.showSection(util.select.position(1, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(1, 2, 0, 1, 2, 1), Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(util.select.position(2, 3, 1), Direction.DOWN);
        scene.idle(5);

        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(new BlockPos(1, 2, 1), Direction.WEST));
        scene.idle(120);

        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame()
            .pointAt(util.vector.blockSurface(new BlockPos(1, 2, 0), Direction.NORTH));
        scene.idle(20);

        scene.effects.rotationDirectionIndicator(util.grid.at(1, 2, 1));
		scene.effects.rotationDirectionIndicator(util.grid.at(1, 2, 0));
        scene.idle(100);

        scene.markAsFinished();
    };

    @SuppressWarnings("resource")
    public static void pumpjack(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("pumpjack", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection pumpjack = util.select.fromTo(1, 1, 3, 3, 2, 3);
        Selection kinetics = util.select.position(3, 1, 4).add(util.select.fromTo(2, 0, 5, 3, 1, 5));
        Selection pipes = util.select.fromTo(2, 1, 1, 3, 1, 2);
        BlockPos pumpjackPos = new BlockPos(2, 1, 3);
        BlockPos pumpPos = new BlockPos(2, 1, 2);

        // Add player
        ElementLink<EntityElement> playerElement = scene.world.createEntity(w -> {
            PonderPlayer player = new PonderPlayer(w, Minecraft.getInstance().player.getScoreboardName());
            Vec3 v = util.vector.topOf(2, 0, 0);
            player.setPos(v.x, v.y, v.z);
            player.xo = v.x;
            player.yo = v.y;
            player.zo = v.z;
            player.setInvisible(true);
            return player;
        });

        // Add TNT
        scene.world.createEntity(w -> {
            PrimedTnt tnt = new PrimedTnt(EntityType.TNT, w);
            Vec3 v = util.vector.topOf(2, 0, 2);
            tnt.setPos(v.x, v.y, v.z);
            tnt.xo = v.x;
            tnt.yo = v.y;
            tnt.zo = v.z;
            tnt.setFuse(80);
            return tnt;
        });

        // Set and then un-set the Player invisible so it it doesn't awkwardly jerk when added to the scene
        scene.world.modifyEntity(playerElement, entity -> {
            if (!(entity instanceof PonderPlayer player)) return;
            player.setItemInHand(InteractionHand.MAIN_HAND, DestroyItems.SEISMOMETER.asStack());
            player.setInvisible(false);
        });

        scene.overlay.showText(60)
            .text("This text is defined in a language file.");
        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(new BlockPos(2, 2, 0), Direction.UP), Pointing.DOWN)
            .withItem(DestroyItems.SEISMOMETER.asStack()),
            60
        );
        scene.idle(80);

        scene.effects.emitParticles(VecHelper.getCenterOf(pumpPos), Emitter.withinBlockSpace(ParticleTypes.EXPLOSION_EMITTER, Vec3.ZERO), 1f, 1);
        scene.world.modifyEntity(playerElement, Entity::discard);
        scene.idle(20);

        scene.world.showSection(kinetics, Direction.NORTH);
        scene.idle(10);
        scene.world.showSection(pumpjack, Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(pumpjackPos, Direction.NORTH))
            .attachKeyFrame();
        scene.idle(120);
        
        scene.world.showSection(pipes, Direction.SOUTH);
        scene.idle(10);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(pumpPos, Direction.UP))
            .attachKeyFrame();
        scene.world.modifyBlockEntity(pumpjackPos, PumpjackBlockEntity.class, be -> {
            be.tank.allowInsertion();
            be.tank.getPrimaryHandler().fill(new FluidStack(DestroyFluids.CRUDE_OIL.get(), 1000), FluidAction.EXECUTE);
            be.tank.forbidInsertion();
        });
        scene.idle(20);
        scene.world.propagatePipeChange(pumpPos);
        scene.idle(100);

        scene.markAsFinished();
    };

    public static void phytomining(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("phytomining", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();

        BlockPos ore = new BlockPos(1, 1, 1);
        BlockPos farmland = new BlockPos(1, 2, 1);

        scene.world.showSection(util.select.fromTo(1, 1, 1, 1, 3, 1), Direction.DOWN);
        scene.idle(10);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(farmland, Direction.UP));
        scene.idle(120);
        scene.overlay.showControls(new InputWindowElement(util.vector.blockSurface(new BlockPos(1, 2, 1), Direction.UP), Pointing.DOWN)
            .rightClick()
            .withItem(new ItemStack(DestroyItems.HYPERACCUMULATING_FERTILIZER.get())),
            30
        );
        scene.idle(60);
        scene.effects.emitParticles(util.vector.topOf(farmland).add(0, 0.25f, 0), Emitter.withinBlockSpace(ParticleTypes.HAPPY_VILLAGER, Vec3.ZERO), 1.0f, 15);
        scene.world.modifyBlock(ore, s -> Blocks.STONE.defaultBlockState(), false);
        scene.world.modifyBlock(new BlockPos(1, 3, 1), s -> DestroyBlocks.GOLDEN_CARROTS.get().defaultBlockState(), false);
        scene.overlay.showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector.blockSurface(ore, Direction.WEST))
            .attachKeyFrame();
        scene.idle(120);
        scene.markAsFinished();
    };

};
