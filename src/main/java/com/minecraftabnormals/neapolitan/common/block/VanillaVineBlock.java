package com.minecraftabnormals.neapolitan.common.block;

import java.util.Optional;
import java.util.Random;

import com.minecraftabnormals.neapolitan.common.block.api.IPoisonCloud;
import com.minecraftabnormals.neapolitan.core.other.NeapolitanTags;
import com.minecraftabnormals.neapolitan.core.registry.NeapolitanBlocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.PlantBlockHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class VanillaVineBlock extends Block implements IPoisonCloud, IGrowable {
    public static final VoxelShape SHAPE = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public VanillaVineBlock(AbstractBlock.Properties properties) {
        super(properties);
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (!state.isValidPosition(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState otherState = worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite()));
        Block block = otherState.getBlock();
        return VanillaVineTopBlock.facingSameDirection(state, otherState) || block.isIn(NeapolitanTags.Blocks.VANILLA_PLANTABLE_ON);
    }

    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == stateIn.get(FACING).getOpposite() && !stateIn.isValidPosition(worldIn, currentPos)) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }

        VanillaVineTopBlock abstracttopplantblock = (VanillaVineTopBlock) NeapolitanBlocks.VANILLA_VINE.get();
        if (facing == stateIn.get(FACING)) {
            Block block = facingState.getBlock();
            if (block != this && block != abstracttopplantblock) {
                return abstracttopplantblock.func_235504_a_(worldIn).with(FACING, stateIn.get(FACING));
            }
        }

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(NeapolitanBlocks.VANILLA_VINE.get());
    }

    public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
        Optional<BlockPos> optional = this.nextGrowPosition(worldIn, pos, state);
        return optional.isPresent() && PlantBlockHelper.isAir(worldIn.getBlockState(optional.get().offset(state.get(FACING))));
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
        return true;
    }

    public void grow(ServerWorld worldIn, Random rand, BlockPos pos, BlockState state) {
        Optional<BlockPos> optional = this.nextGrowPosition(worldIn, pos, state);
        if (optional.isPresent()) {
            BlockState blockstate = worldIn.getBlockState(optional.get());
            ((VanillaVineTopBlock) blockstate.getBlock()).grow(worldIn, rand, optional.get(), blockstate);
        }

    }

    private Optional<BlockPos> nextGrowPosition(IBlockReader reader, BlockPos pos, BlockState state) {
        BlockPos blockpos = pos;

        Block block;
        while (true) {
            blockpos = blockpos.offset(state.get(FACING));
            block = reader.getBlockState(blockpos).getBlock();
            if (block != state.getBlock()) {
                break;
            }
        }

        return block == NeapolitanBlocks.VANILLA_VINE.get() ? Optional.of(blockpos) : Optional.empty();
    }

    @SuppressWarnings("deprecation")
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        boolean flag = super.isReplaceable(state, useContext);
        return flag && useContext.getItem().getItem() == NeapolitanBlocks.VANILLA_VINE.get().asItem() ? false : flag;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(world, pos, state, player);
        this.createPoisonCloud(world, pos, state, player);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }
}